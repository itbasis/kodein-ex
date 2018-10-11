import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformJvmPlugin

apply {
  plugin<KotlinPlatformJvmPlugin>()
}

val kotlinLoggingVersion: String by project
val kodeinVersion: String by project

dependencies {
  "compile"(project(":kodein-ex-common"))

  "implementation"(kotlin("stdlib-jdk8"))
  "implementation"(kotlin("reflect"))

  "compile"("io.github.microutils:kotlin-logging:$kotlinLoggingVersion")
  "compile"("org.kodein.di:kodein-di-generic-jvm:$kodeinVersion")
}
