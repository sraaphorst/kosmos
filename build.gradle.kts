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

// Apply to every subproject that has the Kotlin JVM plugin
subprojects {
    plugins.withId("org.jetbrains.kotlin.jvm") {
        // Kotlin toolchain
        the<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension>().jvmToolchain(21)

        // Common test dependencies
        dependencies {
            val kotestVersion: String by rootProject.extra

            "testImplementation"("io.kotest:kotest-property:$kotestVersion")
            "testImplementation"("io.kotest:kotest-runner-junit5:$kotestVersion")
            "testImplementation"("io.kotest:kotest-assertions-core:$kotestVersion")
        }

        // JUnit5 platform for Kotest
        tasks.withType<Test>().configureEach {
            useJUnitPlatform()
        }
    }
}

extra["kotestVersion"] = "5.9.0"
extra["arrowVersion"]  = "2.1.2"