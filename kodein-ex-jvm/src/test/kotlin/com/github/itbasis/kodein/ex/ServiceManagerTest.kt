package com.github.itbasis.kodein.ex

import io.kotlintest.matchers.collections.contain
import io.kotlintest.matchers.containAll
import io.kotlintest.should
import io.kotlintest.shouldBe
import io.kotlintest.shouldNot
import io.kotlintest.shouldThrow
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters.JVM
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton

abstract class AbstractTestService(private val orders: ArrayList<String>) : AbstractService() {
  override fun start() {
    val serviceName = this.javaClass.simpleName.toLowerCase()
    orders.add(serviceName)
  }
}

class Service01(orders: ArrayList<String>) : AbstractTestService(orders)

class Service02(
  @Suppress("UNUSED_PARAMETER") service03: Service03, orders: ArrayList<String>
               ) : AbstractTestService(orders)

open class Service03(orders: ArrayList<String>) : AbstractTestService(orders)

class Service04(orders: ArrayList<String>) : AbstractTestService(orders) {
  override fun shouldRunAfter() = listOf(Service03::class)
}

class Service05(orders: ArrayList<String>) : Service03(orders)

@FixMethodOrder(JVM)
internal class ServiceManagerTest {
  @Test
  fun testRunExceptionServicesEmpty() {
    shouldThrow<IllegalStateException> {
      val kodein = Kodein {}
      ServiceManager(kodein).run()
    }.message shouldBe "There are no services to run"
  }

  @Test
  fun testRunExceptionMultiple() {
    val orders = ArrayList<String>()
    shouldThrow<IllegalStateException> {
      val kodein = Kodein {
        bind() from singleton { Service01(orders) }
      }
      val serviceManager = ServiceManager(kodein)
      serviceManager.run()
      serviceManager.run()
    }.message shouldBe "It is not allowed to call the method if there are running services"
  }

  @Test
  fun testRunAnyOrderDeclared00() {
    val orders = ArrayList<String>()
    val kodein = Kodein {
      bind() from singleton { Service03(orders) }
      bind() from singleton { Service01(orders) }
    }
    ServiceManager(kodein).run()
    orders should containAll(listOf("service01", "service03"))
  }

  @Test
  fun testRunAnyOrderDeclared01() {
    val orders = ArrayList<String>()
    val kodein = Kodein {
      bind() from singleton { Service01(orders) }
      bind() from singleton { Service03(orders) }
    }
    ServiceManager(kodein).run()
    orders should containAll(listOf("service01", "service03"))
  }

  @Test
  fun testRunAnyOrderDeclared02() {
    val orders = ArrayList<String>()
    val kodein = Kodein {
      bind() from singleton { Service01(orders) }
      bind() from singleton { Service05(orders) }
    }
    ServiceManager(kodein).run()
    orders should containAll(listOf("service01", "service05"))
    orders shouldNot contain("service03")
  }


  @Test
  fun testRunOrderedRun01() {
    val orders = ArrayList<String>()
    val kodein = Kodein {
      bind() from singleton { Service01(orders) }
      bind() from singleton { Service02(instance(), orders) }
      bind() from singleton { Service03(orders) }
    }
    ServiceManager(kodein).run()
    (orders.indexOf("service03") < orders.indexOf("service02")) shouldBe true
  }

  @Test
  fun testRunOrderedRun02() {
    val orders = ArrayList<String>()
    val kodein = Kodein {
      bind() from singleton { Service01(orders) }
      bind() from singleton { Service03(orders) }
      bind() from singleton { Service02(instance(), orders) }
    }
    ServiceManager(kodein).run()
    (orders.indexOf("service03") < orders.indexOf("service02")) shouldBe true
  }

  @Test
  fun testRunOrderedRun03() {
    val orders = ArrayList<String>()
    val kodein = Kodein {
      bind() from singleton { Service01(orders) }
      bind() from singleton { Service03(orders) }
      bind() from singleton { Service04(orders) }
    }
    ServiceManager(kodein).run()
    (orders.indexOf("service03") < orders.indexOf("service04")) shouldBe true
  }

  @Test
  fun testRunOrderedRun04() {
    val orders = ArrayList<String>()
    val kodein = Kodein {
      bind() from singleton { Service01(orders) }
      bind() from singleton { Service04(orders) }
      bind() from singleton { Service03(orders) }
    }
    ServiceManager(kodein).run()
    (orders.indexOf("service03") < orders.indexOf("service04")) shouldBe true
  }

  @Test
  fun testRunOrderedRun05() {
    val orders = ArrayList<String>()
    val kodein = Kodein {
      bind() from singleton { Service01(orders) }
      bind() from singleton { Service04(orders) }
      bind() from singleton { Service05(orders) }
    }
    ServiceManager(kodein).run()
    (orders.indexOf("service05") < orders.indexOf("service04")) shouldBe true
    orders shouldNot contain("service03")
  }

  @Test
  fun testRunOrderedRun06() {
    val orders = ArrayList<String>()
    val kodein = Kodein {
      bind() from singleton { Service01(orders) }
      bind() from singleton { Service02(instance(), orders) }
      bind() from singleton { Service05(orders) }
    }
    ServiceManager(kodein).run()
    (orders.indexOf("service05") < orders.indexOf("service02")) shouldBe true
    orders shouldNot contain("service03")
  }

}
