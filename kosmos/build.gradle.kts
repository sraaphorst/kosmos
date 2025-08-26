plugins {
    kotlin("jvm")
}

group = "org.vorpal"
version = "1.0-SNAPSHOT"

val kotestVersion = rootProject.ext["kotestVersion"]

dependencies {
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("io.kotest:kotest-property:$kotestVersion")
    testImplementation(project(":kosmos-laws"))
    testImplementation(kotlin("test"))
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}