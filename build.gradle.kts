plugins {
    kotlin("jvm") version "2.2.0"

    // Allow sharing helpers by text fixtures.
    id("java-test-fixtures")
}

group = "org.vorpal.kosmos"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val kotestVersion = "5.9.0"


dependencies {
    // Kotest runner + core assertions + property testing
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("io.kotest:kotest-property:$kotestVersion")

    // Optional: kotlin test assertions (not needed if only using kotest)
    testImplementation(kotlin("test"))

    // (Sometimes helpful for IDE run configs)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    testFixturesImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testFixturesImplementation("io.kotest:kotest-property:$kotestVersion")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}