plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "2.2.0"
}

group = "org.vorpal.kosmos.kosmos-testkit"
version = "1.0-SNAPSHOT"

val kotestVersion = rootProject.ext["kotestVersion"]
val serializationVersion = extra["serializationVersion"]

dependencies {
    implementation(project(":kosmos-core"))
    implementation(project(":kosmos-laws"))

    // Kotest property is enough if you’re invoking property checks inside your API.
    // If you also run/spec tests in this module, add runner + assertions to testImplementation.
    implementation("io.kotest:kotest-property:$kotestVersion")

    // Law report integration (org.vorpal.kosmos.testing.laws):
    // - the JSON export of law reports serializes at this boundary, so kosmos-laws needs no JSON dependency;
    // - the nested Kotest adapter registers law graphs as Kotest containers and tests.
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${serializationVersion}")
    implementation("io.kotest:kotest-framework-engine:$kotestVersion")

    // If you have tests *in this module*:
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
}
