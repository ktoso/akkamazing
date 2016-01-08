import sbt._

object Version {
  val akka      = "2.4.1"
  val logback   = "1.1.2"
  val scala     = "2.11.4"
  val scalaTest = "2.2.0"
  val spray     = "1.3.1"
  val sprayJson = "1.2.6"
}

object Library {
  val akkaActor       = "com.typesafe.akka" %% "akka-actor"                    % Version.akka
  val akkaCluster     = "com.typesafe.akka" %% "akka-cluster"                  % Version.akka
  val akkaClusterTools= "com.typesafe.akka" %% "akka-cluster-tools"            % Version.akka
  val akkaContrib     = "com.typesafe.akka" %% "akka-contrib"                  % Version.akka
  val akkaPersistence = "com.typesafe.akka" %% "akka-persistence"              % Version.akka
  val akkaSlf4j       = "com.typesafe.akka" %% "akka-slf4j"                    % Version.akka
  val akkaTestkit     = "com.typesafe.akka" %% "akka-testkit"                  % Version.akka
  val logbackClassic  = "ch.qos.logback"    %  "logback-classic"               % Version.logback
  val scalaTest       = "org.scalatest"     %% "scalatest"                     % Version.scalaTest
  val sprayCan        = "io.spray"          %% "spray-can"                     % Version.spray
  val sprayJson       = "io.spray"          %% "spray-json"                    % Version.sprayJson
  val sprayRouting    = "io.spray"          %% "spray-routing"                 % Version.spray
}

object Dependencies {

  import Library._

  val akkamazing = List(
    akkaActor,
    akkaCluster,
    akkaClusterTools,
    akkaContrib,
    akkaPersistence,
    akkaSlf4j,
    logbackClassic,
    sprayCan,
    sprayJson,
    sprayRouting,
    akkaTestkit % "test",
    scalaTest   % "test"
  )
}
