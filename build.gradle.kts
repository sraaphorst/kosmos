//group = "org.vorpal.kosmos"
//version = "1.0-SNAPSHOT"
//
//allprojects {
//    repositories { mavenCentral() }
//}
//
//subprojects {
//    plugins.withId("org.jetbrains.kotlin.jvm") {
//        tasks.withType<Test>().configureEach {
//            useJUnitPlatform()
//        }
//    }
//}
//
// Root build.gradle.kts

plugins {
    kotlin("jvm") version "2.2.0" apply false
}

allprojects {
    repositories { mavenCentral() }
}

// Configure the Kotlin toolchain only in subprojects that have the Kotlin JVM plugin
subprojects {
    plugins.withId("org.jetbrains.kotlin.jvm") {
        the<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension>().jvmToolchain(21)
    }
}

extra["kotestVersion"] = "5.9.0"
extra["arrowVersion"]  = "2.1.2"