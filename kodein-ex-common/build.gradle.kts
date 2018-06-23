import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformCommonPlugin

apply {
  plugin<KotlinPlatformCommonPlugin>()
}

val kloggingVersion: String by project
val kodeinVersion: String by project

dependencies {
  "compile"(group = "com.github.lewik.klogging", name = "klogging.common", version = kloggingVersion)
  "compile"(group = "org.kodein.di", name = "kodein-di-core-common", version = kodeinVersion)
}
