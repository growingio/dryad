package io.growing.dryad.registry

import java.util.concurrent.{ Executors, TimeUnit }
import java.util.{ ArrayList ⇒ JArrayList, List ⇒ JList }

import com.google.common.util.concurrent.AbstractScheduledService.Scheduler
import com.google.common.util.concurrent.{ AbstractScheduledService, ServiceManager }
import com.orbitz.consul.model.agent.{ ImmutableRegistration, Registration }
import com.typesafe.scalalogging.LazyLogging
import io.growing.dryad.client.ConsulClient
import io.growing.dryad.registry.dto.Service

import scala.collection.JavaConverters._

/**
 * Component:
 * Description:
 * Date: 2016/11/2
 *
 * @author Andy Ai
 */
class ConsulServiceRegistry extends ServiceRegistry with LazyLogging {
  private[this] val ttlServices: JList[Service] = new JArrayList[Service]()
  private val ttlCheckService: AbstractScheduledService = new AbstractScheduledService {
    private lazy val executorService = executor()

    override def runOneIteration(): Unit = {
      ttlServices.asScala.foreach { service ⇒
        executorService.execute(() ⇒ ConsulClient.agentClient.pass(service.id, s"pass in ${System.currentTimeMillis()}"))
      }
    }

    override def scheduler(): Scheduler = Scheduler.newFixedRateSchedule(0, 1, TimeUnit.SECONDS)

    override def shutDown(): Unit = {
      val fixedThreadPool = Executors.newFixedThreadPool(ttlServices.size())
      ttlServices.asScala.foreach { service ⇒
        fixedThreadPool.execute(() ⇒ ConsulClient.agentClient.fail(service.id, s"system shutdown in ${System.currentTimeMillis()}"))
      }
      fixedThreadPool.shutdown()
      fixedThreadPool.awaitTermination(1, TimeUnit.MINUTES)
    }
  }
  private[this] val ttlCheckScheduler: ServiceManager = new ServiceManager(Seq(ttlCheckService).asJava).startAsync()

  Runtime.getRuntime.addShutdownHook(new Thread {
    override def run(): Unit = {
      ttlCheckScheduler.stopAsync()
      ttlCheckScheduler.awaitStopped()
    }
  })

  override def register(service: Service): Unit = {
    val check = service.check match {
      case TTLHealthCheck(ttl)                     ⇒ Registration.RegCheck.ttl(ttl)
      case HttpHealthCheck(url, interval, timeout) ⇒ Registration.RegCheck.http(url, interval, timeout)
      case c                                       ⇒ throw new UnsupportedOperationException(s"Unsupported check: ${c.getClass.getName}")
    }
    val basicTags: Seq[String] = Seq(
      s"""type = "microservice"""",
      s"priority = ${service.priority}",
      s"""group = "${service.group}"""",
      s"""schema = "${service.schema}"""",
      s"""pattern = "${service.pattern}"""")
    lazy val nonCertifications = if (service.nonCertifications.nonEmpty) {
      Option(s"""non_certifications = "${service.nonCertifications.mkString(",")}"""")
    } else {
      None
    }
    val optionalTags = Seq(
      nonCertifications,
      service.loadBalancing.map(lb ⇒ s"""load_balancing = "$lb"""")).collect {
        case Some(tag) ⇒ tag
      }
    val tags = basicTags ++ optionalTags
    val registration = ImmutableRegistration.builder()
      .id(service.id)
      .name(service.name)
      .address(service.address)
      .port(service.port)
      .addTags(tags: _*)
      .enableTagOverride(false)
      .check(check).build()
    ConsulClient.agentClient.register(registration)
    if (service.check.isInstanceOf[TTLHealthCheck]) {
      ttlServices.add(service)
    }
  }

  override def deregister(serviceId: String): Unit = {
    ttlServices.asScala.find(s ⇒ s.id == serviceId).foreach { s ⇒
      ttlServices.remove(s)
      ConsulClient.agentClient.fail(serviceId)
    }
    ConsulClient.agentClient.deregister(serviceId)
  }

}

