import Dependencies._

lazy val options = Seq(
  //"-encoding",
  "UTF-8",
  "-feature", // emit warning and location for usages of features that should be imported explicitly
  "-deprecation", // emit warning and location for usages of deprecated APIs
  //"-explain",   // explain errors in more detail
  //"-explain-types",
  "-unchecked", // enable additional warnings where generated code depends on assumptions
  "-language:postfixOps",
  "-language:higherKinds", // or import scala.language.higherKinds
  //"-new-syntax",
  //"-Xmigration",
  //"-print-lines",
  //"-rewrite",
  //"-indent",
  //"-source:3.1"
)

lazy val jvmOptions = Seq()

lazy val jvmcOptions = Seq("-source", "19")

lazy val testsFrameworks = Seq(
  new TestFramework("weaver.framework.CatsEffect")
)

lazy val commonSettings = Seq(
  version                          := "0.1",
  organization                     := "com.leysoft",
  scalaVersion                     := scala3Version,
  scalacOptions                    := options,
  javaOptions                      := jvmOptions,
  javacOptions                     := jvmcOptions,
  Test / fork                      := true,
  Test / testForkedParallel        := true,
  Test / parallelExecution         := true,
  Test / scalaSource               := baseDirectory.value / "src/test/scala",
  ThisBuild / scalafmtOnCompile    := true,
  ThisBuild / autoCompilerPlugins  := true,
  assembly / assemblyMergeStrategy := {
    case PathList("META-INF",_ @ _*) => MergeStrategy.discard
    case _                           => MergeStrategy.first
  }
)

lazy val itSettings = Seq(
  IntegrationTest / fork               := true,
  IntegrationTest / testForkedParallel := true,
  IntegrationTest / parallelExecution  := false,
  IntegrationTest / scalaSource        := baseDirectory.value / "src/it/scala",
)

lazy val plugins =
  sys.env.get("ENV") match {
    case Some("DEV")  => Seq(SbtDotenv)
    case Some("PROD") => Seq()
    case _            => Seq(SbtDotenv)
  }

lazy val itConfig =
  inConfig(IntegrationTest)(
    Defaults.itSettings ++ ScalafmtPlugin.scalafmtConfigSettings
  )

lazy val testConfig =
  inConfig(Test)(
    Defaults.testSettings ++ ScalafmtPlugin.scalafmtConfigSettings
  )

lazy val root = (project in file("."))
  .settings(commonSettings: _*)
  .settings(
    name := "dotty-http"
  )
  .configs(Test)
  .settings(testConfig)
  .configs(IntegrationTest)
  .settings(itConfig)
  .enablePlugins(plugins: _*)
  .aggregate(
    core,
    infrastructure,
    products,
    api,
    commands
  )

lazy val api = (project in file("api"))
  .settings(commonSettings: _*)
  .settings(
    name    := "api",
    version := "0.1.0-SNAPSHOT"
  )
  .configs(Test)
  .settings(testConfig)
  .configs(IntegrationTest)
  .settings(itConfig)
  .settings(itSettings: _*)
  .enablePlugins(FlywayPlugin)
  .enablePlugins(plugins: _*)
  .settings(
    libraryDependencies ++= Seq(
      monocle,
      squants,
      kittens,
      cats("cats-kernel"),
      cats("cats-core"),
      cats("cats-free"),
      catsMtl,
      catsEffect,
      circe("circe-core"),
      circe("circe-generic"),
      circe("circe-parser"),
      fs2("fs2-core"),
      fs2("fs2-io"),
      logback,
      logbackEncoder,
      log4Cats("log4cats-core"),
      log4Cats("log4cats-slf4j"),
      http4s("http4s-dsl"),
      http4s("http4s-blaze-server"),
      http4s("http4s-blaze-client"),
      http4s("http4s-circe"),
      groovy
    ),
    Compile / mainClass        := Some("com.leysoft.api.Api"),
    assembly / mainClass       := Some("com.leysoft.api.Api"),
    assembly / assemblyJarName := "api.jar",
    flywayUrl                  := sys
      .env
      .getOrElse(
        "DATABASE_URL",
        "jdbc:postgresql://localhost:5432/database"
      ),
    flywayUser     := sys.env.getOrElse("DATABASE_USER", "postgres"),
    flywayPassword := sys.env.getOrElse("DATABASE_PASSWORD", "postgres"),
    flywayLocations += "db/migration"
  )
  .dependsOn(
    kernel,
    logger,
    httpKernel,
    server,
    client,
    sql,
    memory,
    kafka
  )

lazy val commands = (project in file("commands"))
  .settings(commonSettings: _*)
  .settings(
    name    := "commands",
    version := "0.1.0-SNAPSHOT"
  )
  .configs(Test)
  .settings(testConfig)
  .configs(IntegrationTest)
  .settings(itConfig)
  .settings(itSettings: _*)
  .enablePlugins(plugins: _*)
  .settings(
    libraryDependencies ++= Seq(
      monocle,
      squants,
      kittens,
      cats("cats-kernel"),
      cats("cats-core"),
      cats("cats-free"),
      catsMtl,
      catsEffect,
      circe("circe-core"),
      circe("circe-generic"),
      circe("circe-parser"),
      fs2("fs2-core"),
      fs2("fs2-io"),
      logback,
      logbackEncoder,
      log4Cats("log4cats-core"),
      log4Cats("log4cats-slf4j"),
      http4s("http4s-dsl"),
      http4s("http4s-blaze-client"),
      http4s("http4s-circe"),
      groovy
    ),
    Compile / mainClass        := Some("com.leysoft.commands.Consumer"),
    assembly / mainClass       := Some("com.leysoft.commands.Consumer"),
    assembly / assemblyJarName := "commands.jar",
  )
  .dependsOn(
    kernel,
    logger,
    httpKernel,
    client,
    sql,
    memory,
    kafka
  )

lazy val products = (project in file("products"))
  .settings(commonSettings: _*)
  .settings(
    name := "products",
    scalacOptions ++= options
  )
  .aggregate(
    productsDomain,
    productsApplications,
    productsAdapters
  )

lazy val productsDomain = (project in file("products/domain"))
  .settings(commonSettings: _*)
  .configs(Test)
  .settings(testConfig)
  .settings(
    name := "products-domain",
    scalacOptions ++= options,
    libraryDependencies ++= Seq(
      cats("cats-kernel"),
      cats("cats-core"),
      cats("cats-free"),
      catsMtl,
      monocle,
      squants,
      fs2("fs2-core")
    )
  )
  .dependsOn(
    kernel % "compile->compile;test->test",
    logger
  )

lazy val productsApplications = (project in file("products/application"))
  .settings(commonSettings: _*)
  .configs(Test)
  .settings(testConfig)
  .settings(
    name := "products-application",
    scalacOptions ++= options,
    libraryDependencies ++= Seq(
      cats("cats-kernel"),
      cats("cats-core"),
      cats("cats-free"),
      catsMtl,
      monocle,
      fs2("fs2-core")
    )
  )
  .dependsOn(
    kernel % "compile->compile;test->test",
    logger,
    productsDomain % "compile->compile;test->test"
  )

lazy val productsAdapters = (project in file("products/adapters"))
  .settings(commonSettings: _*)
  .configs(Test)
  .settings(testConfig)
  .settings(
    name := "products-adapters",
    scalacOptions ++= options,
    libraryDependencies ++= Seq(
      cats("cats-kernel"),
      cats("cats-core"),
      cats("cats-free"),
      catsMtl,
      monocle,
      fs2("fs2-core"),
      circe("circe-core"),
      circe("circe-generic"),
      circe("circe-parser"),
      http4s("http4s-dsl"),
      http4s("http4s-blaze-server"),
      http4s("http4s-blaze-client"),
      http4s("http4s-circe")
    )
  )
  .dependsOn(
    kernel % "compile->compile;test->test",
    logger,
    productsDomain % "compile->compile;test->test",
    httpKernel,
    server,
    client,
    sql,
    memory,
    kafka,
  )

lazy val core = (project in file("core"))
  .settings(commonSettings: _*)
  .settings(
    name := "core",
    scalacOptions ++= options
  )
  .aggregate(
    kernel,
    logger
  )

lazy val kernel = (project in file("core/kernel"))
  .settings(commonSettings: _*)
  .configs(Test)
  .settings(testConfig)
  .settings(
    name := "kernel",
    scalacOptions ++= options,
    libraryDependencies ++= Seq(
      cats("cats-core"),
      cats("cats-kernel"),
      catsMtl,
      monocle,
      squants,
      kittens,
      fs2("fs2-core"),
      circe("circe-core"),
      circe("circe-generic"),
      circe("circe-parser"),
      ciris("ciris"),
      jacksonScala,
      scalatest % Test,
      scalacheck % Test,
    )
  )

lazy val logger = (project in file("core/logger"))
  .settings(commonSettings: _*)
  .configs(Test)
  .settings(testConfig)
  .settings(
    name := "logger",
    scalacOptions ++= options,
    libraryDependencies ++= Seq(
      cats("cats-core"),
      cats("cats-kernel"),
      catsMtl,
      fs2("fs2-core"),
      monix("monix-execution"),
      monocle,
      squants,
      kittens,
      logback,
      log4Cats("log4cats-core"),
      log4Cats("log4cats-slf4j"),
      groovy
    )
  )
  .dependsOn(kernel)

lazy val infrastructure = (project in file("infrastructure"))
  .settings(commonSettings: _*)
  .settings(
    name := "infrastructure",
    scalacOptions ++= options
  )
  .aggregate(
    http,
    database,
    message
  )

lazy val http = (project in file("infrastructure/http"))
  .settings(commonSettings: _*)
  .settings(
    name := "http",
    scalacOptions ++= options
  )
  .aggregate(
    httpKernel,
    server,
    client
  )

lazy val httpKernel = (project in file("infrastructure/http/kernel"))
  .settings(commonSettings: _*)
  .configs(Test)
  .settings(testConfig)
  .settings(
    name := "http-kernel",
    scalacOptions ++= options,
    libraryDependencies ++= Seq(
      cats("cats-kernel"),
      cats("cats-core"),
      cats("cats-free"),
      catsMtl,
      catsEffect,
      fs2("fs2-core"),
      http4s("http4s-dsl")
    )
  )
  .dependsOn(
    kernel % "compile->compile;test->test",
    logger
  )

lazy val server = (project in file("infrastructure/http/server"))
  .settings(commonSettings: _*)
  .configs(Test)
  .settings(testConfig)
  .settings(
    name := "http-server",
    scalacOptions ++= options,
    testFrameworks ++= testsFrameworks,
    libraryDependencies ++= Seq(
      cats("cats-kernel"),
      cats("cats-core"),
      cats("cats-free"),
      catsMtl,
      catsEffect,
      refined("refined"),
      refined("refined-cats"),
      ciris("ciris"),
      ciris("ciris-refined"),
      circe("circe-core"),
      circe("circe-generic"),
      circe("circe-parser"),
      fs2("fs2-core"),
      fs2("fs2-io"),
      http4s("http4s-dsl"),
      http4s("http4s-blaze-server"),
      http4s("http4s-circe"),
      log4Cats("log4cats-core"),
      scalatest % Test,
      weaver("weaver-cats") % Test
    )
  )
  .dependsOn(
    kernel % "compile->compile;test->test",
    logger,
    httpKernel % "compile->compile;test->test"
  )

lazy val client = (project in file("infrastructure/http/client"))
  .settings(commonSettings: _*)
  .configs(Test)
  .settings(testConfig)
  .configs(IntegrationTest)
  .settings(itConfig)
  .settings(itSettings: _*)
  .settings(
    name := "http-client",
    scalacOptions ++= options,
    testFrameworks ++= testsFrameworks,
    libraryDependencies ++= Seq(
      cats("cats-kernel"),
      cats("cats-core"),
      cats("cats-free"),
      catsMtl,
      catsEffect,
      refined("refined"),
      refined("refined-cats"),
      ciris("ciris"),
      ciris("ciris-refined"),
      circe("circe-core"),
      circe("circe-generic"),
      circe("circe-parser"),
      fs2("fs2-core"),
      fs2("fs2-io"),
      http4s("http4s-dsl"),
      http4s("http4s-blaze-client"),
      http4sJdkHttpClient,
      http4s("http4s-circe"),
      scalatest % Test,
      scalacheck % Test,
      weaver("weaver-cats") % Test,
      weaver("weaver-scalacheck") % Test
    )
  )
  .dependsOn(
    kernel % "compile->compile;test->test",
    logger,
    httpKernel  % "compile->compile;test->test"
  )

lazy val database = (project in file("infrastructure/database"))
  .settings(commonSettings: _*)
  .settings(
    name := "database",
    scalacOptions ++= options
  )
  .aggregate(
    sql,
    memory
  )

lazy val sql = (project in file("infrastructure/database/sql"))
  .settings(commonSettings: _*)
  .configs(Test)
  .settings(testConfig)
  .configs(IntegrationTest)
  .settings(itConfig)
  .settings(itSettings: _*)
  .settings(
    name := "database-sql",
    scalacOptions ++= options,
    libraryDependencies ++= Seq(
      cats("cats-kernel"),
      cats("cats-core"),
      cats("cats-free"),
      catsMtl,
      catsEffect,
      refined("refined"),
      refined("refined-cats"),
      ciris("ciris"),
      ciris("ciris-refined"),
      circe("circe-core"),
      circe("circe-generic"),
      circe("circe-parser"),
      fs2("fs2-core"),
      fs2("fs2-io"),
      ciris("ciris"),
      ciris("ciris-refined"),
      doobie("doobie-core"),
      doobie("doobie-hikari"),
      doobie("doobie-postgres"),
      skunk("skunk-core"),
      skunk("skunk-circe")
    )
  )
  .dependsOn(
    kernel % "compile->compile;test->test",
    logger
  )

lazy val memory = (project in file("infrastructure/database/memory"))
  .settings(commonSettings: _*)
  .configs(Test)
  .settings(testConfig)
  .configs(IntegrationTest)
  .settings(itConfig)
  .settings(itSettings: _*)
  .settings(
    name := "database-memory",
    scalacOptions ++= options,
    libraryDependencies ++= Seq(
      cats("cats-kernel"),
      cats("cats-core"),
      cats("cats-free"),
      catsMtl,
      catsEffect,
      refined("refined"),
      refined("refined-cats"),
      ciris("ciris"),
      ciris("ciris-refined"),
      circe("circe-core"),
      circe("circe-generic"),
      circe("circe-parser"),
      fs2("fs2-core"),
      fs2("fs2-io"),
      redis("redis4cats-effects"),
      redis("redis4cats-streams"),
      redis("redis4cats-log4cats")
    )
  )
  .dependsOn(
    kernel % "compile->compile;test->test",
    logger
  )

lazy val message = (project in file("infrastructure/message-broker"))
  .settings(commonSettings: _*)
  .settings(
    name := "message-broker",
    scalacOptions ++= options
  )
  .aggregate(
    kafka
  )

lazy val kafka = (project in file("infrastructure/message-broker/kafka"))
  .settings(commonSettings: _*)
  .configs(Test)
  .settings(testConfig)
  .configs(IntegrationTest)
  .settings(itConfig)
  .settings(itSettings: _*)
  .settings(
    name := "kafka",
    scalacOptions ++= options,
    libraryDependencies ++= Seq(
      cats("cats-kernel"),
      cats("cats-core"),
      cats("cats-free"),
      catsMtl,
      catsEffect,
      refined("refined"),
      refined("refined-cats"),
      ciris("ciris"),
      ciris("ciris-refined"),
      circe("circe-core"),
      circe("circe-generic"),
      circe("circe-parser"),
      fs2("fs2-core"),
      fs2("fs2-io"),
      fs2Kafka("fs2-kafka"),
      kafkaStreams,
      jacksonScala,
      jacksonModule("jackson-datatype-jdk8"),
      jacksonModule("jackson-datatype-jsr310")
    )
  )
  .dependsOn(
    kernel % "compile->compile;test->test",
    logger
  )

addCommandAlias(
  "exec",
  ";clean;update;scalafmtCheckAll;compile;test;it:test"
)
