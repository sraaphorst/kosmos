plugins { kotlin("jvm") }
group = "org.vorpal.kosmos.algorithms"
version = "1.0-SNAPSHOT"

// Some algorithms will require Lawful instances, so we want kosmos-lawkit as a dependency here.
// TODO: re-add kosmos-lawkit once it is working.
dependencies {
    implementation(project(":kosmos-core"))
    implementation(project(":kosmos-testkit"))
//    implementation(project(":kosmos-lawkit"))
}
