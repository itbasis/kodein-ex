import org.gradle.plugins.ide.idea.model.IdeaModel

val kotlinVersion: String by extra
val kloggingVersion: String by extra

repositories {
  jcenter()
  mavenCentral()
  maven(url = "https://jitpack.io")
}

configurations.all {
  resolutionStrategy {
    failOnVersionConflict()

    eachDependency {
      when (requested.group) {
        "org.jetbrains.kotlin" -> useVersion(kotlinVersion)
        "org.slf4j"            -> useVersion("+")
        "junit"                -> useVersion("4.+")
        "io.kotlintest"        -> useVersion("+")
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
