import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestLogEvent.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin ("jvm") version "1.9.23"
  application
  id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "com.gonzazago.nauta"
version = "1.0.0-SNAPSHOT"

repositories {
  mavenCentral()
}

val vertxVersion = "4.5.16"
val junitJupiterVersion = "5.9.1"

val mainVerticleName = "com.gonzazago.nauta.orders.MainVerticle"
val launcherClassName = "io.vertx.core.Launcher"

val watchForChange = "src/**/*"
val doOnChange = "${projectDir}/gradlew classes"

application {
  mainClass.set(launcherClassName)
}

dependencies {
  implementation(platform("io.vertx:vertx-stack-depchain:$vertxVersion"))
  implementation("io.insert-koin:koin-core:3.5.3")
  implementation("io.vertx:vertx-web")
  implementation("io.vertx:vertx-mysql-client")
  implementation("io.vertx:vertx-lang-kotlin-coroutines")
  implementation("io.vertx:vertx-lang-kotlin")
  implementation("com.fasterxml.jackson.core:jackson-databind:2.17.1")
  implementation("com.typesafe:config:1.4.2")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.1")
  implementation(kotlin("stdlib-jdk8"))

  implementation("io.vertx:vertx-jdbc-client:$vertxVersion")
  implementation("io.vertx:vertx-mongo-client:$vertxVersion")

  implementation("com.h2database:h2:2.2.224")
  implementation("io.agroal:agroal-api:1.15")
  implementation("io.agroal:agroal-pool:1.15")

  testImplementation("io.vertx:vertx-core:$vertxVersion")
  testImplementation("io.vertx:vertx-junit5")
  testImplementation("org.junit.jupiter:junit-jupiter:$junitJupiterVersion")
  testImplementation("io.mockk:mockk:1.13.7")
  testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")





}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions.jvmTarget = "17"

tasks.withType<ShadowJar> {
  archiveClassifier.set("fat")
  manifest {
    attributes(mapOf("Main-Verticle" to mainVerticleName))
  }
  mergeServiceFiles()
}

tasks.withType<Test> {
  useJUnitPlatform()
  testLogging {
    events = setOf(PASSED, SKIPPED, FAILED)
  }
}

tasks.withType<JavaExec> {
  args = listOf("run", mainVerticleName, "--redeploy=$watchForChange", "--launcher-class=$launcherClassName", "--on-redeploy=$doOnChange")
}
