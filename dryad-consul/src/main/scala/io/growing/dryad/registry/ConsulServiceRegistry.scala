package io.growing.dryad.registry

import java.util.concurrent.{Executors, TimeUnit}
import java.util.{ArrayList ⇒ JArrayList, List ⇒ JList}

import com.google.common.util.concurrent.AbstractScheduledService.Scheduler
import com.google.common.util.concurrent.{AbstractScheduledService, ServiceManager}
import com.orbitz.consul.model.agent.{ImmutableRegistration, Registration}
import io.growing.dryad.client.ConsulClient
import io.growing.dryad.registry.dto.Service

import scala.collection.JavaConversions._

/**
 * Component:
 * Description:
 * Date: 2016/11/2
 *
 * @author Andy Ai
 */
class ConsulServiceRegistry extends ServiceRegistry {
  private[this] val services: JList[Service] = new JArrayList[Service]()
  private val executorService: AbstractScheduledService = new AbstractScheduledService {
    private lazy val executorService = executor()

    override def runOneIteration(): Unit = {
      services.foreach { service ⇒
        executorService.execute(new Runnable {
          override def run(): Unit = ConsulClient.agentClient.pass(service.id, s"pass in ${System.currentTimeMillis()}")
        })
      }
    }

    override def scheduler(): Scheduler = Scheduler.newFixedRateSchedule(0, 1, TimeUnit.SECONDS)

    override def shutDown(): Unit = {
      val fixedThreadPool = Executors.newFixedThreadPool(services.size())
      services.foreach { service ⇒
        fixedThreadPool.execute(new Runnable {
          override def run(): Unit = ConsulClient.agentClient.fail(service.id, s"system shutdown in ${System.currentTimeMillis()}")
        })
      }
      fixedThreadPool.shutdown()
      fixedThreadPool.awaitTermination(1, TimeUnit.MINUTES)
    }
  }
  private[this] val scheduler: ServiceManager = new ServiceManager(Seq(executorService)).startAsync()

  Runtime.getRuntime.addShutdownHook(new Thread {
    override def run(): Unit = {
      scheduler.stopAsync()
      scheduler.awaitStopped()
    }
  })

  override def register(service: Service): Unit = {
    val ttlCheck = Registration.RegCheck.ttl(service.ttl)
    val tags: Seq[String] = Seq(
      s"group = ${service.group}",
      s"schema = ${service.schema}",
      s"pattern = ${service.pattern}"
    )
    val registration = ImmutableRegistration.builder()
      .id(service.id)
      .name(service.name)
      .address(service.address)
      .port(service.port)
      .addTags(tags: _*)
      .enableTagOverride(false)
      .check(ttlCheck).build()
    ConsulClient.agentClient.register(registration)
    services.add(service)
  }

  override def deregister(serviceId: String): Unit = {
    services.find(s ⇒ s.id == serviceId).foreach(s ⇒ services.remove(s))
    ConsulClient.agentClient.fail(serviceId)
    ConsulClient.agentClient.deregister(serviceId)
  }

}

