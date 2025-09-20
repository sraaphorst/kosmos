//pluginManagement {
//    plugins {
//        id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
//        kotlin("jvm") version "2.2.0"
//    }
//    repositories {
//        gradlePluginPortal()
//        mavenCentral()
//    }
//}
//
//rootProject.name = "kosmos"
//
//include(":kosmos-core")
//project(":kosmos-core").projectDir = file("kosmos-core")
//
//include(":kosmos-lawkit")
//project(":kosmos-lawkit").projectDir = file("kosmos-lawkit")
//
//include(":kosmos-laws")
//project(":kosmos-laws").projectDir = file("kosmos-laws")
rootProject.name = "kosmos"
include(":kosmos-core", ":kosmos-laws", ":kosmos-lawkit")