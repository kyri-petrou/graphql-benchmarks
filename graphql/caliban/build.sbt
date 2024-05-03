ThisBuild / scalaVersion     := "3.4.1"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

lazy val root = (project in file("."))
  .settings(
    name       := "scala-caliban",
    run / fork := true,
    run / javaOptions ++= Seq("-Xms4G", "-Xmx4G"),
    libraryDependencies ++= Seq(
      "com.github.ghostdogpr"                 %% "caliban-quick"         % "2.6.0",
      "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core"   % "2.28.5",
      "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % "2.28.5" % Provided,
      "org.apache.httpcomponents.client5"      % "httpclient5"           % "5.3.1",
      "dev.zio"                               %% "zio"                   % "2.1.0-RC5",
      "dev.zio"                               %% "zio-http"              % "3.0.0-RC6+37-65dc08d7-SNAPSHOT"
    )
  )

resolvers ++= Resolver.sonatypeOssRepos("snapshots")
