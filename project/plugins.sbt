resolvers += Resolver.url("git://github.com/jrudolph/sbt-dependency-graph.git")

//resolvers += Resolver.url("sbt-plugin-releases",
//  new URL("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases/"))(Resolver.ivyStylePatterns)

resolvers += Resolver.url("git://github.com/sbt/sbt-onejar.git")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.6.0")

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.2.0")

addSbtPlugin("com.github.retronym" % "sbt-onejar" % "0.8")