Dryad
==================

[![Build Status](https://travis-ci.org/growingio/dryad.svg?branch=master)](https://travis-ci.org/growingio/dryad)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.growing/dryad-core_2.12/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.growing/dryad-core_2.12)

Dryad 是一个配置文件管理客户端，它提供了配置文件的热加载等功能。并且在不改变原有 Ref 的情况下实现数据的变更。

## 获取

### Consul

```
"io.growing" %% "dryad-consul" % "1.0.0-SNAPSHOT"
```

## 使用

**application.conf**

```conf
dryad {

  namespace = "default"
  group = "prod"

  provider = "io.growing.dryad.provider.ConsulConfigProvider"

  consul {
    host = "localhost"
    port = 8500
  }
}
```

**Case Class**

```scala
@Configuration(name = "env.conf", parser = classOf[DevConfigParser])
case class DevConfig(name: String, group: String)

class DevConfigParser extends ConfigParser[DevConfig] {
  override def parse(config: Config): DevConfig = Configs[DevConfig].extract(config).value
}
```

**Sample**

```scala
val configSystem = ConfigSystem()
val devConfig = configSystem.get[DevConfig]

//devConfig 可以作为一个全局的常量，因为 devConfig 是一个 Config 的 Ref, 当配置文件更新时这个 Ref 会指向新的对象(配置文件更新之后所生成的对象)。
```
