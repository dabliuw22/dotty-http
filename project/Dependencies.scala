import sbt._

object Dependencies {
  lazy val scala3Version = "3.1.0"

  def cats(artifact: String): ModuleID =
    "org.typelevel" %% artifact % "2.7.0"
  lazy val catsEffect: ModuleID =
    "org.typelevel" %% "cats-effect" % "3.3.1"
  lazy val catsMtl: ModuleID = "org.typelevel" %% "cats-mtl" % "1.2.1"
  def monix(artifact: String): ModuleID =
    "io.monix" %% artifact % "3.4.0"
  def refined(artifact: String): ModuleID =
    "eu.timepit" %% artifact % "0.9.28"
  lazy val shapeless3: ModuleID =
    "org.typelevel" %% "shapeless3-deriving" % "3.0.4"
  lazy val kittens: ModuleID =
    "org.typelevel" %% "kittens" % "3.0.0-M1"
  lazy val monixNewtype: ModuleID =
    "io.monix" %% "newtypes-core" % "0.0.1"
  lazy val monocle: ModuleID =
    "dev.optics" %% "monocle-core" % "3.0.0"
  lazy val squants: ModuleID = "org.typelevel" %% "squants" % "1.8.3"
  def fs2(artifact: String): ModuleID = "co.fs2" %% artifact % "3.2.2"
  def doobie(artifact: String): ModuleID =
    "org.tpolecat" %% artifact % "1.0.0-RC1"
  def skunk(artifact: String): ModuleID =
    "org.tpolecat" %% artifact % "0.2.3"
  def redis(artifact: String): ModuleID =
    "dev.profunktor" %% artifact % "1.0.0"
  def http4s(artifact: String): ModuleID =
    "org.http4s" %% artifact % "1.0.0-M30"
  lazy val http4sJdkHttpClient = 
    "org.http4s" %% "http4s-jdk-http-client" % "0.6.0-M7"
  def circe(artifact: String): ModuleID =
    "io.circe" %% artifact % "0.14.1"
  lazy val jacksonScala: ModuleID =
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.13.0"
  def jacksonModule(artifact: String): ModuleID =
    "com.fasterxml.jackson.datatype" % artifact % "2.13.0"
  def log4Cats(artifact: String): ModuleID =
    "org.typelevel" %% artifact % "2.1.1"
  lazy val logback: ModuleID =
    "ch.qos.logback" % "logback-classic" % "1.2.9"
  lazy val logbackEncoder: ModuleID =
    "net.logstash.logback" % "logstash-logback-encoder" % "7.0.1"
  def fs2Kafka(artifact: String): ModuleID =
    "com.github.fd4s" %% artifact % "2.2.0"
  lazy val kafkaStreams: ModuleID =
    "org.apache.kafka" % "kafka-streams" % "3.0.0"
  def ciris(artifact: String): ModuleID =
    "is.cir" %% artifact % "2.3.1"
  lazy val catsEffectTesting: ModuleID =
    "org.typelevel" %% "cats-effect-testing-scalatest" % "1.4.0"
  lazy val groovy: ModuleID =
    "org.codehaus.groovy" % "groovy-all" % "3.0.9"
  lazy val scalatest: ModuleID = "org.scalatest" %% "scalatest" % "3.2.10"
  lazy val scalacheck: ModuleID =
    "org.scalacheck" %% "scalacheck" % "1.15.4"
  lazy val munit: ModuleID =
    "org.typelevel" %% "munit-cats-effect-3" % "1.0.6"
  def weaver(artifact: String): ModuleID = "com.disneystreaming" %% artifact % "0.7.7"
}
