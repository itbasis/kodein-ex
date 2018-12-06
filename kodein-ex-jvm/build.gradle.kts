import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformJvmPlugin

apply {
  plugin<KotlinPlatformJvmPlugin>()
}

val kotlinVersion = rootProject.extra["kotlin.version"] as String
val klogVersion = rootProject.extra["klog.version"] as String
val kodeinVersion= rootProject.extra["kodein.version"] as String
val kotlintestVersion = extra["kotlintest.version"] as String
val slf4jVersion = extra["slf4j.version"] as String

dependencies {
  "expectedBy"(project(":kodein-ex-common"))

  "compile"(kotlin("stdlib-jdk8", kotlinVersion))
  "compile"(kotlin("reflect", kotlinVersion))

  "compile"("com.github.lewik.klog:klog-jvm:$klogVersion")
  "compile"("org.kodein.di:kodein-di-generic-jvm:$kodeinVersion")

  "testImplementation"("org.slf4j:slf4j-simple:$slf4jVersion")
  "testImplementation"(kotlin("test-junit5", kotlinVersion))
  "testImplementation"("io.kotlintest:kotlintest-extensions-system:$kotlintestVersion")
  "testImplementation"("io.kotlintest:kotlintest-assertions-arrow:$kotlintestVersion")
  "testImplementation"("io.kotlintest:kotlintest-runner-junit4:$kotlintestVersion")
}
