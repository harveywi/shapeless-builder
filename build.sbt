lazy val shapelessBuilder = (project in file(".")
  aggregate(core, examples)
  dependsOn(core, examples)
  settings(commonSettings: _*)
  settings(moduleName := "shapeless-builder-root"))

lazy val core = (project
  settings(commonSettings: _*)
  settings(moduleName := "shapeless-builder"))


lazy val examples = (project
  dependsOn core
  settings(commonSettings: _*)
  settings(moduleName := "shapeless-builder-examples"))

def commonSettings =
  Seq(
    organization := "org.aylasoftware",
    scalaVersion := "2.12.4",
    crossScalaVersions := Seq(scalaVersion.value, "2.10.7", "2.11.12", "2.13.0-M3"),
    scalacOptions := Seq(
      "-feature",
      "-language:higherKinds",
      "-Xfatal-warnings",
      "-deprecation",
      "-unchecked"
    ),

    resolvers ++= Seq(
      Resolver.sonatypeRepo("releases"),
      Resolver.sonatypeRepo("snapshots")
    ),

    libraryDependencies ++= Seq(
      "com.chuusai" %% "shapeless" % "2.3.3" withSources(),
      "org.scalatest" %% "scalatest" % "3.0.5-M1" % "test"
    )
  )