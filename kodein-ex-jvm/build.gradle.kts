import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformJvmPlugin

apply {
  plugin<KotlinPlatformJvmPlugin>()
}

val kloggingVersion: String by project
val kodeinVersion: String by project

dependencies {
  "compile"(project(":kodein-ex-common"))

  "implementation"(kotlin("stdlib-jdk8"))
  "implementation"(kotlin("reflect"))

  "compile"(group = "com.github.lewik.klogging", name = "klogging.jvm", version = kloggingVersion) {
    exclude(group = "com.github.lewik")
  }

  "compile"(group = "org.kodein.di", name = "kodein-di-generic-jvm", version = kodeinVersion)
//  "compile"(group = "org.kodein.di", name = "kodein-di-conf-jvm", version = kodeinVersion)
}
