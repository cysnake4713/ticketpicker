name := "tickpicker"

version := "1.0"

scalaVersion := "2.9.2"

resolvers ++= Seq(
    "Maven Repository" at "http://repo1.maven.org/maven2/",
    "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
)

libraryDependencies ++= Seq(
    "com.typesafe.akka" % "akka-actor" % "2.0.5",
    "com.typesafe.akka" % "akka-slf4j" % "2.0.5",
    "org.apache.httpcomponents" % "httpclient" % "4.2.3",
    "org.scala-lang" % "scala-swing" % "2.9.2",
    "org.json" % "json" % "20090211",
 //   "log4j" % "log4j" % "1.2.17"
    "ch.qos.logback" % "logback-classic" % "1.0.9"
)
