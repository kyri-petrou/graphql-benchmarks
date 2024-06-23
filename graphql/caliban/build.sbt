ThisBuild / scalaVersion     := "3.4.2"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

lazy val root = (project in file("."))
  .settings(
    name       := "scala-caliban",
    run / fork := true,
    run / javaOptions ++= Seq("-Xms8G", "-Xmx8G"),
    libraryDependencies ++= Seq(
      "com.github.ghostdogpr"                 %% "caliban-quick"         % "2.7.2",
      "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core"   % "2.30.1",
      "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % "2.30.1" % Provided,
      "org.apache.httpcomponents.client5"      % "httpclient5"           % "5.3.1",
      "dev.zio"                               %% "zio"                   % "2.1.4",
      "dev.zio"                               %% "zio-query"             % "0.7.1+6-f79ed5a9-SNAPSHOT"
    )
  )

resolvers ++= Resolver.sonatypeOssRepos("snapshots")
