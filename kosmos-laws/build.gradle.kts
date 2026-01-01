plugins { kotlin("jvm") }
group = "org.vorpal.kosmos.kosmos-laws"
version = "1.0-SNAPSHOT"

val kotestVersion = rootProject.ext["kotestVersion"]

dependencies {
    implementation(project(":kosmos-core"))
    implementation("io.kotest:kotest-property:$kotestVersion")
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
}
