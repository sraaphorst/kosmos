plugins { kotlin("jvm") }
group = "org.vorpal.kosmos.kosmos-testkit"
version = "1.0-SNAPSHOT"

val arrowVersion = rootProject.ext["arrowVersion"]
val kotestVersion = rootProject.ext["kotestVersion"]

dependencies {
    implementation(project(":kosmos-core"))

    // Kotest property is enough if you’re invoking property checks inside your API.
    // If you also run/spec tests in this module, add runner + assertions to testImplementation.
    implementation("io.kotest:kotest-property:$kotestVersion")

    // If you have tests *in this module*:
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")

    // We use arrow for validation.
    implementation("io.arrow-kt:arrow-core:${arrowVersion}")
}
