ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.5.1"

lazy val root = (project in file("."))
  .settings(
    name := "tdt4165-scala-project",
    idePackagePrefix := Some("no.ntnu.tdt4165")
  )
