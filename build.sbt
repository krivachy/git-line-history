import com.typesafe.sbt.SbtNativePackager._
import NativePackagerKeys._

name := "git-line-history"

version := "1.0"

scalaVersion := "2.11.5"

libraryDependencies += "org.clapper" %% "argot" % "1.0.3"

libraryDependencies += "org.scalatest" % "scalatest_2.11" % "2.2.1" % "test"

libraryDependencies += "org.mockito" % "mockito-all" % "1.10.19"

libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.3"

libraryDependencies += "commons-io" % "commons-io" % "2.4"

// Build:

packageArchetype.java_application
