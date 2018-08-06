package com.github.itbasis.kodein.ex

import klogging.KLogger
import klogging.KLoggers
import klogging.WithLogging
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.allInstances
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.jvmErasure
import kotlin.system.measureTimeMillis

class ServiceManager(override val kodein: Kodein) : KodeinAware,
                                                    WithLogging {

  override val logger: KLogger
    get() = KLoggers.logger(this)

  private val services: List<Service>
    get() {
      val t: List<Service> by kodein.allInstances()
      return t
    }

  private val servicesStarted = mutableListOf<Service>()
  private val servicesFinalizedAfterStart = mutableListOf<Service>()

  private fun isRunning(): Boolean = servicesStarted.isNotEmpty()

  fun run() {
    check(!isRunning()) { "It is not allowed to call the method if there are running services" }
    check(services.isNotEmpty()) { "There are no services to run" }

    val time = measureTimeMillis {
      logger.info { "services: $services" }
      services.forEach { service -> treeRun(service) }
      (services as ArrayList<*>).removeAll(servicesFinalizedAfterStart)
      servicesFinalizedAfterStart.onEach {
        it.stop()
      }.clear().run {
        System.gc()
      }
    }
    logger.info { "Started within $time milliseconds" }
  }

  fun stop() {
    val time = measureTimeMillis {
      stopServices(servicesFinalizedAfterStart)
      stopServices(servicesStarted)
    }
    logger.info { "Started within $time milliseconds" }
  }

  private fun stopServices(services: MutableList<Service>) {
    services.onEach { service ->
      val serviceClassName = service::class.simpleName
      try {
        logger.info { "Service '$serviceClassName': stopping..." }
        service.stop()
        logger.info { "Service '$serviceClassName': stopped" }
      } catch (e: Throwable) {
        logger.error(e) { "Service '$serviceClassName': stop fail" }
      }
    }.clear()
  }

  private fun treeRun(service: Service): Boolean {
    if (service in servicesStarted) return true
    if (service in servicesFinalizedAfterStart) return true

    serviceDependencies(service).forEach { treeRun(it) }

    val serviceClassName = service::class.simpleName
    try {
      logger.info { "Service '$serviceClassName': starting..." }
      service.start()

      when (service.finalizeAfterStart) {
        true -> servicesFinalizedAfterStart.add(service)
        else -> servicesStarted.add(service)
      }

      logger.info { "Service '$serviceClassName': started" }
    } catch (e: Throwable) {
      logger.error(e) { "Service '$serviceClassName': start fail" }
      stop()
      return false
    }
    return true
  }

  private fun serviceDependencies(service: Service): Collection<Service> {
    val result = hashSetOf<Service>()

    service.shouldRunAfter()?.flatMap { serviceKClazz ->
      services.filter { serviceKClazz.isInstance(it) }
    }?.run {
      result.addAll(this)
    }

    with(service::class) {
      constructors.flatMap {
        it.parameters
      }.filter {
        it.type.jvmErasure.isSubclassOf(Service::class)
      }.flatMap { filterType ->
        services.filter { filterType.type.jvmErasure.isInstance(it) }
      }.run {
        result.addAll(this)
      }

      members.filter {
        it.returnType.jvmErasure.isSubclassOf(Service::class)
      }.flatMap { filterType ->
        services.filter { filterType.returnType.jvmErasure.isInstance(it) }
      }.run {
        result.addAll(this)
      }
    }
    return result
  }
}
