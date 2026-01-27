plugins { kotlin("jvm") }
group = "org.vorpal.kosmos.noise"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(project(":kosmos-core"))
    implementation(project(":kosmos-testkit"))
}
