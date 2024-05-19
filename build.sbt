ThisBuild / version := "1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.14"

lazy val root = (project in file("."))
  .settings(
    name := "itcube-scala"
  )

libraryDependencies += "dev.zio" %% "zio" % "2.1.1"
libraryDependencies += "dev.zio" %% "zio-json" % "0.6.2"
libraryDependencies += "dev.zio" %% "zio-http" % "3.0.0-RC6"

libraryDependencies += "dev.zio" %% "zio-config" % "4.0.2"
libraryDependencies += "dev.zio" %% "zio-config-typesafe" % "4.0.2"
libraryDependencies += "dev.zio" %% "zio-config-magnolia" % "4.0.2"

libraryDependencies += "io.getquill" %% "quill-zio" % "4.8.4"
libraryDependencies += "io.getquill" %% "quill-jdbc-zio" % "4.8.4"

libraryDependencies += "org.postgresql" % "postgresql" % "42.7.3"
