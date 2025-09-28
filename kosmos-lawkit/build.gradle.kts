plugins { kotlin("jvm") }
group = "org.vorpal.kosmos.lawkit"
version = "1.0-SNAPSHOT"

val arrowVersion = rootProject.ext["arrowVersion"]
val kotestVersion = rootProject.ext["kotestVersion"]

// TODO: This needs seriously cleaning up. I don't think we need kotest as an implementation here,
// TODO: and the testImplementations should be included by the project-level build.gradle.kts.
dependencies {
    implementation(project(":kosmos-core"))
    implementation(project(":kosmos-laws"))

    // Kotest property is enough if you’re invoking property checks inside your API.
    // If you also run/spec tests in this module, add runner + assertions to testImplementation.
    implementation("io.kotest:kotest-property:$kotestVersion")

    // We use arrow for validation.
    implementation("io.arrow-kt:arrow-core:${arrowVersion}")

    // If you have tests *in this module*:
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
}
