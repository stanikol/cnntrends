name := "cnn-trends"

version := "1.0"

//scalaVersion := "2.13.1"
scalaVersion := "2.12.10"

lazy val akkaVersion = "2.6.0"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-http"   % "10.1.10",
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "org.scalatest" %% "scalatest" % "3.0.8" % Test
)

// https://mvnrepository.com/artifact/com.rometools/rome
//libraryDependencies += "com.rometools" % "rome" % "1.12.2"

// https://mvnrepository.com/artifact/org.scala-lang.modules/scala-xml
libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "2.0.0-M1"

// https://mvnrepository.com/artifact/org.seleniumhq.selenium/selenium-java
libraryDependencies += "org.seleniumhq.selenium" % "selenium-java" % "3.141.59"

// https://mvnrepository.com/artifact/org.seleniumhq.selenium/selenium-firefox-driver
libraryDependencies += "org.seleniumhq.selenium" % "selenium-firefox-driver" % "3.141.59"

libraryDependencies ++= Seq(
  "com.typesafe.slick" %% "slick" % "3.3.1",
  "org.slf4j" % "slf4j-nop" % "1.7.26",
//  "ch.qos.logback" % "logback-classic" % "1.2.3",
//  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.3.1"
)

// https://mvnrepository.com/artifact/com.lightbend.akka/akka-stream-alpakka-slick
//libraryDependencies += "com.lightbend.akka" %% "akka-stream-alpakka-slick" % "1.1.2"
libraryDependencies += "com.lightbend.akka" %% "akka-stream-alpakka-slick" % "2.0.0-M1"

resolvers += "typesafe" at "http://repo.typesafe.com/typesafe/releases/"

// https://mvnrepository.com/artifact/com.h2database/h2
libraryDependencies += "com.h2database" % "h2" % "1.4.200"

enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)

mainClass in Compile := Some("org.cnntrends.Main")

dockerBaseImage       := "cnn-trends-base"
dockerExposedPorts ++= Seq(9000, 8080, 80)

