package io.growing.dryad.internal.impl

import java.nio.file.{ Files, Path, Paths, StandardOpenOption }
import java.util.concurrent.Callable
import java.util.concurrent.atomic.AtomicReference

import com.google.common.base.Charsets
import com.google.common.cache.CacheBuilder
import com.typesafe.config.{ Config, ConfigFactory, ConfigRef }
import io.growing.dryad.annotation.Configuration
import io.growing.dryad.internal.{ ConfigService, ConfigurationDesc }
import io.growing.dryad.parser.ConfigParser
import io.growing.dryad.provider.ConfigProvider
import io.growing.dryad.snapshot.LocalFileConfigSnapshot
import io.growing.dryad.watcher.ConfigChangeListener
import net.sf.cglib.proxy.Enhancer

import scala.reflect.ClassTag
import scala.util.{ Failure, Success, Try }

/**
 * Component:
 * Description:
 * Date: 16/3/26
 *
 * @author Andy Ai
 */
class ConfigServiceImpl(provider: ConfigProvider) extends ConfigService {

  private[this] val separator = "/"
  private[this] val objects = CacheBuilder.newBuilder().build[String, AnyRef]()
  private[this] val configs = CacheBuilder.newBuilder().build[String, Config]()

  override def download(root: Path, path: String): Path = {
    val config = provider.load(path)
    val paths = path.split(separator)
    val directory = Paths.get(root.toString, paths.dropRight(1): _*)
    if (Files.notExists(directory)) {
      Files.createDirectories(directory)
    }
    val filePath = directory.resolve(paths.last)
    Files.write(filePath, config.payload.getBytes(Charsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
  }

  override def get[T: ClassTag](namespace: String, group: String): T = {
    val clazz = implicitly[ClassTag[T]].runtimeClass
    objects.get(clazz.getName, new Callable[AnyRef] {
      override def call(): AnyRef = createObjectRef(clazz, namespace, group)
    }).asInstanceOf[T]
  }

  override def get(name: String, namespace: String, group: Option[String]): Config = {
    val path = getPath(name, namespace, group)
    configs.get(path, new Callable[Config] {
      override def call(): Config = {
        val refreshAndFlush = (configuration: ConfigurationDesc, ref: AtomicReference[Config]) ⇒ {
          ref.set(ConfigFactory.parseString(configuration.payload))
          LocalFileConfigSnapshot.flash(configuration)
        }
        val underlying: AtomicReference[Config] = new AtomicReference[Config]()
        val c = provider.load(path, new ConfigChangeListener {
          override def onChange(configuration: ConfigurationDesc): Unit = {
            refreshAndFlush(configuration, underlying)
          }
        })
        refreshAndFlush(c, underlying)
        new ConfigRef(underlying)
      }
    })
  }

  override def getConfigAsString(name: String, namespace: String, group: Option[String]): String = {
    provider.load(getPath(name, namespace, group)).payload
  }

  override def getConfigAsStringRecursive(path: String, namespace: String, group: String): String = {
    var result: Try[ConfigurationDesc] = null
    val paths = path.split(separator)
    val name = paths.last
    (Seq(namespace, group) ++ paths.dropRight(1)).inits.exists {
      case Nil ⇒ false
      case segments: Seq[String] ⇒
        val configName = (segments :+ name).mkString(separator)
        result = Try(provider.load(configName))
        result.isSuccess
    }
    result match {
      case Success(config) ⇒ config.payload
      case Failure(t)      ⇒ throw t
    }
  }

  private[this] def createObjectRef(clazz: Class[_], namespace: String, group: String): AnyRef = {
    val refreshAndFlush = (configuration: ConfigurationDesc, ref: ObjectRef, parser: ConfigParser[_]) ⇒ {
      val config = ConfigFactory.parseString(configuration.payload)
      ref.reference.set(parser.parse(config))
      LocalFileConfigSnapshot.flash(configuration)
    }
    val annotation = clazz.getAnnotation(classOf[Configuration])
    val ref: ObjectRef = new ObjectRef(new AtomicReference[Any]())
    val parser = annotation.parser().newInstance()
    val path = getPath(annotation.name(), namespace, if (annotation.ignoreGroup()) None else Option(group))
    val configuration = provider.load(path, new ConfigChangeListener {
      override def onChange(configuration: ConfigurationDesc): Unit = {
        refreshAndFlush(configuration, ref, parser)
      }
    })
    refreshAndFlush(configuration, ref, parser)

    val enhancer = new Enhancer()
    enhancer.setSuperclass(clazz)
    enhancer.setCallbackType(classOf[ObjectRef])
    enhancer.setCallback(ref)
    val parameterTypes: Array[Class[_]] = clazz.getConstructors.head.getParameterTypes
    createObject(enhancer, parameterTypes)
  }

  private def createObject(enhancer: Enhancer, parameterTypes: Array[Class[_]]): AnyRef = {
    enhancer.create(parameterTypes, parameterTypes.map { c ⇒
      val v = c.getName match {
        case "byte"    ⇒ 0
        case "short"   ⇒ 0
        case "int"     ⇒ 0
        case "long"    ⇒ 0L
        case "float"   ⇒ 0.0f
        case "double"  ⇒ 0.0d
        case "char"    ⇒ '\u0000'
        case "boolean" ⇒ false
        case _         ⇒ null
      }
      v.asInstanceOf[AnyRef]
    })
  }

  def getPath(name: String, namespace: String, group: Option[String] = None): String = {
    val paths = group.fold(Seq(namespace, name))(_group ⇒ Seq(namespace, _group, name))
    paths.filterNot(_.trim.isEmpty).mkString(separator)
  }

}
