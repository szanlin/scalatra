resolvers ++= Seq(
  Resolver.url("Typesafe repository", new java.net.URL("http://typesafe.artifactoryonline.com/typesafe/ivy-releases/"))(Resolver.defaultIvyPatterns)
)

libraryDependencies <+= sbtVersion(v => "com.github.siasia" %% "xsbt-web-plugin" % (v+"-0.2.10"))

addSbtPlugin("net.databinder" % "posterous-sbt" % "0.3.2")

resolvers += "Akka Repository" at "http://akka.io/repository"

addSbtPlugin("com.typesafe.akka" % "akka-sbt-plugin" % "2.0-M4")
