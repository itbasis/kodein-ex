import io.gitlab.arturbosch.detekt.DetektPlugin
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.jetbrains.kotlin.gradle.dsl.Coroutines
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformCommonPlugin
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformJsPlugin
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformJvmPlugin
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformPluginBase
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import org.gradle.language.base.plugins.LifecycleBasePlugin.BUILD_TASK_NAME
import org.gradle.language.base.plugins.LifecycleBasePlugin.ASSEMBLE_TASK_NAME
import org.gradle.plugins.ide.idea.model.IdeaModel

tasks.withType<Wrapper> {
  distributionType = Wrapper.DistributionType.ALL
  gradleVersion = "5.0"
}

buildscript {
  val kotlinVersion = extra["kotlin.version"] as String

  repositories {
    jcenter()
    gradlePluginPortal()
  }

  dependencies {
    classpath(kotlin("gradle-plugin", kotlinVersion))
    classpath("gradle.plugin.io.gitlab.arturbosch.detekt:detekt-gradle-plugin:latest.release")
  }
}

apply {
  plugin<IdeaPlugin>()
  plugin<MavenPlugin>()
  plugin<MavenPublishPlugin>()
}

version = if (version != "unspecified") version else file("versions.txt").readLines().first().substringAfter("=")

allprojects {
  group = "ru.itbasis.kodein-ex"

  apply {
    plugin<IdeaPlugin>()
  }

  configure<IdeaModel> {
    module {
      isDownloadJavadoc = false
      isDownloadSources = false
    }
  }

  repositories {
    mavenLocal()
    jcenter()
    maven(url = "https://jitpack.io")
  }

  configurations.all {
    resolutionStrategy {
      failOnVersionConflict()

      eachDependency {
        when (requested.group) {
          "org.jetbrains.kotlin" -> useVersion(extra["kotlin.version"] as String)
        }
      }
    }
  }
}

subprojects {
  version = rootProject.version

  apply {
    plugin<BasePlugin>()
    plugin<DetektPlugin>()
  }

  afterEvaluate {
    plugins.withType(JavaBasePlugin::class.java) {
      configure<JavaPluginConvention> {
        sourceCompatibility = JavaVersion.VERSION_1_8
      }
    }

    tasks.withType(KotlinCompile::class.java) {
      kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
      }
    }

    plugins.withType(KotlinPlatformPluginBase::class.java) {
      rootProject.configure<PublishingExtension> {
        publications {
          create(project.name, MavenPublication::class.java) {
            artifactId = project.name
            from(project.components["java"])
          }
        }
      }
    }

    tasks.withType(Test::class.java).all {
      failFast = true
      useJUnit()
      testLogging {
        showStandardStreams = true
      }
    }
  }

  rootProject.tasks[BUILD_TASK_NAME].shouldRunAfter(tasks[BUILD_TASK_NAME])
}

tasks.create("generateVersion").apply {
  group = LifecycleBasePlugin.BUILD_GROUP
  tasks[ASSEMBLE_TASK_NAME].shouldRunAfter(this)
  doLast {
    version = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"))
    subprojects.forEach { it.version = version }
    file("versions.txt").writeText("version=$version")
  }
}
tasks[BUILD_TASK_NAME].apply {
  doLast {
    subprojects.forEach { subProject ->
      logger.lifecycle("subProject: '${subProject.group}:${subProject.name}:${subProject.version}'")
      copy {
        from("${subProject.buildDir}/libs")
        into("$buildDir/libs")
      }
    }
  }
}
