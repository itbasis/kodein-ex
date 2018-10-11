import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformCommonPlugin

apply {
  plugin<KotlinPlatformCommonPlugin>()
}

val kotlinLoggingVersion: String by project
val kodeinVersion: String by project

dependencies {
  "compile"("io.github.microutils:kotlin-logging-common:$kotlinLoggingVersion")
  "compile"("org.kodein.di:kodein-di-core-common:$kodeinVersion")
}
