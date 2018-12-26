package io.growing.dryad.git2consul.cache

import java.nio.file.Paths

import org.ehcache.config.builders.{ CacheConfigurationBuilder, CacheManagerBuilder, ResourcePoolsBuilder }
import org.ehcache.config.units.{ EntryUnit, MemoryUnit }
import org.ehcache.{ Cache, CacheManager }

/**
 * Component:
 * Description:
 * Date: 2018-12-26
 *
 * @author AI
 */
object Caches {

  private[this] lazy val versionDefaultSize = 100
  private[this] lazy val cacheManager: CacheManager = {
    val manager = CacheManagerBuilder.newCacheManagerBuilder()
      .`with`(CacheManagerBuilder.persistence(Paths.get(System.getProperty("user.home"), ".git2consul").toFile))
      .withCache("versions", CacheConfigurationBuilder
        .newCacheConfigurationBuilder(classOf[String], classOf[String], ResourcePoolsBuilder.newResourcePoolsBuilder()
          .heap(versionDefaultSize, EntryUnit.ENTRIES)
          .disk(versionDefaultSize, MemoryUnit.MB, true))).build(true)
    Runtime.getRuntime.addShutdownHook(new Thread() {
      override def run(): Unit = manager.close()
    })
    manager
  }

  lazy val versions: Cache[String, String] = cacheManager.getCache("versions", classOf[String], classOf[String])

}
