name := """BaiTH3"""
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.13.18"

libraryDependencies ++= Seq(
  guice,
  "com.microsoft.sqlserver" % "mssql-jdbc" % "12.10.1.jre11"
)