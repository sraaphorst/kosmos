// Root build.gradle.kts — no plugins here
group = "org.vorpal.kosmos"
version = "1.0-SNAPSHOT"

allprojects {
    repositories { mavenCentral() }
}

subprojects {
    plugins.withId("org.jetbrains.kotlin.jvm") {
        tasks.withType<Test>().configureEach {
            useJUnitPlatform()
        }
    }
}

// Centralized versions:
ext["kotestVersion"] = "5.9.0"
ext["arrowVersion"] = "2.1.2"