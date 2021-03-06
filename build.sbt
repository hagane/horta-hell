name := "horta-hell"

version := "0.1-SNAPSHOT"

mainClass in (Compile, run) := Some("ru.org.codingteam.horta.Application")

scalaVersion := "2.10.0"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

resolvers ++= Seq(
	"clojars" at "http://clojars.org/repo/",
	"clojure-releases" at "http://build.clojure.org/releases")

resolvers += "Local Maven Repository" at "file:///"+Path.userHome+"/.m2/repository"

libraryDependencies += "platonus" % "platonus" % "0.1.19"

libraryDependencies += "com.typesafe.akka" % "akka-actor_2.10" % "2.2.1"

libraryDependencies += "org.igniterealtime.smack" % "smack" % "3.2.1"

libraryDependencies += "org.igniterealtime.smack" % "smackx" % "3.2.1"

libraryDependencies += "javax.transaction" % "jta" % "1.1"

libraryDependencies += "com.github.scala-incubator.io" %% "scala-io-core" % "0.4.1"

libraryDependencies += "com.github.scala-incubator.io" %% "scala-io-file" % "0.4.1"

libraryDependencies += "com.h2database" % "h2" % "1.3.173"

libraryDependencies += "joda-time" % "joda-time" % "2.3"

libraryDependencies += "org.joda" % "joda-convert" % "1.5"
