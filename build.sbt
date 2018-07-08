import xerial.sbt.Sonatype._

val unusedRepo = Some(Resolver.file("Unused transient repository", file("target/unusedrepo")))

val publishSettings = Seq(
  publishTo := sonatypePublishTo.value,
  publishArtifact in Test := false,
  publishMavenStyle := true,
  licenses := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")),
  sonatypeProjectHosting := Some(GitHubHosting("fomkin", "levsha", "Aleksey Fomkin", "aleksey.fomkin@gmail.com"))
)

val dontPublishSettings = Seq(
  publish := {},
  publishTo := unusedRepo,
  publishArtifact := false
)

val commonSettings = Seq(
  organization := "com.github.fomkin",
  git.useGitDescribe := true,
  //scalaVersion := "2.12.4", // Need by IntelliJ
  scalacOptions ++= Seq(
    "-deprecation",
    "-feature",
    "-Xfatal-warnings",
    "-Xexperimental",
    "-unchecked"
  ),
  testFrameworks += new TestFramework("utest.runner.Framework"),
  libraryDependencies ++= Seq(
    "com.lihaoyi" %% "utest" % "0.4.5" % "test",
    "org.scalacheck" %% "scalacheck" % "1.13.4" % "test"
  )
)

lazy val core = crossProject
  .crossType(CrossType.Pure)
  .enablePlugins(GitVersioning)
  .settings(commonSettings: _*)
  .settings(publishSettings: _*)
  .settings(
    normalizedName := "levsha-core",
    libraryDependencies ++= Seq(
      // Macro compat
      "org.typelevel" %% "macro-compat" % "1.1.1" % "provided",
      "com.lihaoyi" %%% "fastparse" % "1.0.0" % "provided",
      "org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided",
      compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
    )
  )

lazy val coreJS = core.js
lazy val coreJVM = core.jvm

lazy val events = crossProject
  .crossType(CrossType.Pure)
  .enablePlugins(GitVersioning)
  .settings(commonSettings: _*)
  .settings(publishSettings: _*)
  .settings(normalizedName := "levsha-events")
  .dependsOn(core)

lazy val eventsJS = events.js
lazy val eventsJVM = events.jvm

lazy val dom = project
  .enablePlugins(ScalaJSPlugin)
  .enablePlugins(GitVersioning)
  .settings(commonSettings: _*)
  .settings(publishSettings: _*)
  .dependsOn(coreJS)
  .dependsOn(eventsJS)
  .settings(
    normalizedName := "levsha-dom",
    //scalaJSUseMainModuleInitializer := true,
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.9.3"
    )
  )

lazy val bench = project
  .enablePlugins(JmhPlugin)
  .enablePlugins(SbtTwirl)
  .settings(commonSettings: _*)
  .settings(dontPublishSettings: _*)
  .dependsOn(coreJVM)
  .settings(
    normalizedName := "levsha-bench",
    libraryDependencies ++= Seq(
      "com.lihaoyi" %% "scalatags" % "0.6.5"
    )
  )

lazy val root = project
  .in(file("."))
  .settings(commonSettings:_*)
  .settings(dontPublishSettings:_*  )
  .settings(normalizedName := "levsha")
  .aggregate(
    coreJS, coreJVM,
    eventsJS, eventsJVM,
    dom
  )

crossScalaVersions := Seq("2.11.11", "2.12.4")
