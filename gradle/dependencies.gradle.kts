import org.gradle.plugins.ide.idea.model.IdeaModel

val kotlinVersion: String by extra
val kloggingVersion: String by extra

repositories {
  mavenLocal()
  jcenter()
  mavenCentral()
  maven(url = "https://jitpack.io")
}

configurations.all {
  resolutionStrategy {
    failOnVersionConflict()

    eachDependency {
      when (requested.group) {
        "org.jetbrains.kotlin"      -> useVersion(kotlinVersion)
        "org.slf4j"                 -> useVersion("1.7.25")
        "junit"                     -> useVersion("4.12")
        "io.kotlintest"             -> useVersion("3.1.6")
//        "org.kodein.di"             -> useVersion("5.1.0")
//        "com.github.lewik.klogging" -> useVersion("1.2.41")
//        "com.github.lewik"          -> useTarget("com.github.lewik.klogging:${requested.name}:$kloggingVersion")
      }
    }
  }
}

apply {
  plugin<IdeaPlugin>()
}

configure<IdeaModel> {
  module {
    isDownloadJavadoc = false
    isDownloadSources = false
  }
}
