package com.github.itbasis.kodein.ex

import klogging.KLogger
import klogging.KLoggers
import klogging.WithLogging
import org.kodein.di.bindings.ScopeCloseable
import kotlin.reflect.KClass

interface Service : ScopeCloseable,
                    WithLogging {
  override val logger: KLogger
    get() = KLoggers.logger(this)

  @Throws(Throwable::class)
  fun start()

  @Throws(Throwable::class)
  fun stop() = Unit

  override fun close() = stop()

  val finalizeAfterStart: Boolean
    get() = false

  /**
   * If you only need to specify the classes of services from which the dependency occurs (for example, for Weak-services)
   */
  fun shouldRunAfter(): List<KClass<out Service>>? = emptyList()
}
