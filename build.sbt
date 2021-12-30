import Dependencies._

lazy val options = Seq(
  "UTF-8",
  "-feature", // emit warning and location for usages of features that should be imported explicitly
  "-deprecation", // emit warning and location for usages of deprecated APIs
  //"-explain",       // explain errors in more detail
  //"-explain-types", // explain type errors in more detail
  //"-indent",        // allow significant indentation.
  //"-new-syntax",    // require `then` and `do` in control expressions.
  //"-print-lines",   // show source code line numbers.
  "-unchecked", // enable additional warnings where generated code depends on assumptions
  //"-Ykind-projector", // allow `*` as wildcard to be compatible with kind projector
  //"-source:future", // for { (x, given String) <- eff }
  //"-Xfatal-warnings", // fail the compilation if there are any warnings
  //"-Xmigration", // warn about constructs whose behavior may have changed since version
  "-language:postfixOps",
  "-language:higherKinds" // or import scala.language.higherKinds
)

lazy val jvmOptions = Seq()

lazy val commonSettings = Seq(
  version                               := "0.1",
  organization                          := "com.leysoft",
  scalaVersion                          := scala3Version,
  scalacOptions                         := options,
  javaOptions                           := jvmOptions,
  fork in Test                          := true,
  testForkedParallel in Test            := true,
  parallelExecution in Test             := true,
  fork in IntegrationTest               := true,
  testForkedParallel in IntegrationTest := true,
  parallelExecution in IntegrationTest  := false,
  scalaSource in Test := baseDirectory.value / "src/test/scala",
  scalaSource in IntegrationTest := baseDirectory.value / "src/it/scala",
  scalafmtOnCompile in ThisBuild    := true,
  autoCompilerPlugins in ThisBuild  := true,
  assemblyMergeStrategy in assembly := {
    case PathList("META-INF",_ @ _*) => MergeStrategy.discard
    case _                         => MergeStrategy.first
  }
)

lazy val plugins =
  sys.env.get("ENV") match {
    case Some("DEV")  => Seq(SbtDotenv)
    case Some("PROD") => Seq()
    case _            => Seq(SbtDotenv)
  }

lazy val root = (project in file("."))
  .settings(commonSettings: _*)
  .settings(
    name    := "dotty-http",
    version := "0.1.0-SNAPSHOT"
  )
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings))
  .settings(
    inConfig(IntegrationTest)(ScalafmtPlugin.scalafmtConfigSettings)
  )
  .configs(Test)
  .enablePlugins(FlywayPlugin)
  .settings(inConfig(Test)(Defaults.testSettings))
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
      http4s("http4s-circe")
    ),
    mainClass in Compile        := Some("com.leysoft.ContextualMain"),
    mainClass in assembly       := Some("com.leysoft.ContextualMain"),
    assemblyJarName in assembly := "main.jar",
    flywayUrl                   := sys
      .env
      .getOrElse(
        "DB_URL",
        "jdbc:postgresql://localhost:5432/http4s_db"
      ),
    flywayUser     := sys.env.getOrElse("DB_USER", "http4s"),
    flywayPassword := sys.env.getOrElse("DB_PASSWORD", "http4s"),
    flywayLocations += "db/migration"
  ).enablePlugins(plugins: _*)
  .aggregate(core, infrastructure)
  .dependsOn(
    kernel,
    logger,
    infrastructure,
    database,
    memory
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
      jacksonModule("jackson-datatype-jdk8"),
      jacksonModule("jackson-datatype-jsr310")
    )
  )

lazy val logger = (project in file("core/logger"))
  .settings(commonSettings: _*)
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
    database,
  )

lazy val database = (project in file("infrastructure/database"))
  .settings(commonSettings: _*)
  .settings(
    name := "database",
    scalacOptions ++= options
  )
  .aggregate(
    memory
  )

lazy val memory = (project in file("infrastructure/database/memory"))
  .settings(commonSettings: _*)
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
    kernel,
    logger
  )

addCommandAlias(
  "exec",
  ";clean;update;scalafmtCheckAll;compile;test;it:test"
)