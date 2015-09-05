import sbt._
import Keys._

object ShapelessBuilderBuild extends Build {

  lazy val shapelessBuilder = (project in file(".")
    aggregate(core, examples)
    dependsOn(core, examples)
    settings(commonSettings: _*)
    settings(
      moduleName := "shapeless-builder-root"
    )
  )

  lazy val core = (project
    settings(commonSettings: _*)
    settings(
      moduleName := "shapeless-builder"
    )
  )


  lazy val examples = (project
    dependsOn core
    settings(commonSettings: _*)
    settings(
      moduleName := "shapeless-builder-examples"
    )
  )

  def commonSettings = 
    Seq(
      organization := "org.aylasoftware",
      scalaVersion := "2.11.7",
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
        "com.chuusai" %% "shapeless" % "2.2.5" withSources(),
        "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test"
      )
    )
}
