name := "tickpicker"

version := "1.0"

scalaVersion := "2.9.2"

resolvers ++= Seq(
    "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
    "Maven Repository" at "http://repo1.maven.org/maven2/"
)

libraryDependencies ++= Seq(
    "com.typesafe.akka" % "akka-actor" % "2.0.5",
    "org.apache.httpcomponents" % "httpclient" % "4.2.3",
    "org.scala-lang" % "scala-swing" % "2.9.2"
)
