package io.growing.dryad.registry

import java.util.concurrent.{ Executors, TimeUnit }
import java.util.{ ArrayList ⇒ JArrayList, List ⇒ JList }

import com.google.common.util.concurrent.AbstractScheduledService.Scheduler
import com.google.common.util.concurrent.{ AbstractScheduledService, ServiceManager }
import com.orbitz.consul.model.agent.{ ImmutableRegistration, Registration }
import com.typesafe.scalalogging.LazyLogging
import io.growing.dryad.client.ConsulClient
import io.growing.dryad.registry.dto.{ Portal, Service }

import scala.collection.JavaConverters._

/**
 * Component:
 * Description:
 * Date: 2016/11/2
 *
 * @author Andy Ai
 */
class ConsulServiceRegistry extends ServiceRegistry with LazyLogging {
  private[this] val ttlPortals: JList[Portal] = new JArrayList[Portal]()
  private val ttlCheckService: AbstractScheduledService = new AbstractScheduledService {
    @volatile private lazy val executorService = executor()

    override def runOneIteration(): Unit = {
      ttlPortals.asScala.foreach { portal ⇒
        executorService.execute(() ⇒ ConsulClient.agentClient.pass(portal.id, s"pass in ${System.currentTimeMillis()}"))
      }
    }

    override def scheduler(): Scheduler = Scheduler.newFixedRateSchedule(0, 1, TimeUnit.SECONDS)

    override def shutDown(): Unit = {
      val fixedThreadPool = Executors.newFixedThreadPool(ttlPortals.size())
      ttlPortals.asScala.foreach { portal ⇒
        fixedThreadPool.execute(() ⇒ ConsulClient.agentClient.fail(portal.id, s"system shutdown in ${System.currentTimeMillis()}"))
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
    service.portals.foreach { portal ⇒
      val check = portal.check match {
        case TTLHealthCheck(ttl)                     ⇒ Registration.RegCheck.ttl(ttl)
        case HttpHealthCheck(url, interval, timeout) ⇒ Registration.RegCheck.http(url, interval, timeout)
        case GrpcHealthCheck(grpc, interval, useTls) ⇒ Registration.RegCheck.grpc(grpc, interval, useTls)
        case c                                       ⇒ throw new UnsupportedOperationException(s"Unsupported check: ${c.getClass.getName}")
      }
      val basicTags: Seq[String] = Seq(
        s"""type = "microservice"""",
        s"priority = ${service.priority}",
        s"""group = "${service.group}"""",
        s"""schema = "${portal.schema}"""",
        s"""pattern = "${portal.pattern}"""")
      @volatile lazy val nonCertifications = if (portal.nonCertifications.nonEmpty) {
        Option(s"""non_certifications = "${portal.nonCertifications.mkString(",")}"""")
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
        .id(portal.id)
        .name(s"${service.name}-${portal.schema}")
        .address(service.address)
        .port(portal.port)
        .addTags(tags: _*)
        .enableTagOverride(false)
        .check(check).build()
      ConsulClient.agentClient.register(registration)
      if (portal.check.isInstanceOf[TTLHealthCheck]) {
        ttlPortals.add(portal)
      }
    }
  }

  override def deregister(service: Service): Unit = {
    service.portals.foreach { portal ⇒
      ttlPortals.asScala.find(_.id == portal.id).foreach { s ⇒
        ttlPortals.remove(s)
        ConsulClient.agentClient.fail(portal.id)
      }
      ConsulClient.agentClient.deregister(portal.id)
    }
  }

}

