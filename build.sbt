name := """AmazonProject"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  anorm,
  "com.amazonaws" % "aws-java-sdk-s3" % "1.9.28.1",
  "com.amazonaws" % "aws-java-sdk-sqs" % "1.9.28.1",
  "com.amazonaws" % "aws-java-sdk-core" % "1.9.28.1",
  "mysql" % "mysql-connector-java" % "5.1.18"
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
//routesGenerator := InjectedRoutesGenerator
