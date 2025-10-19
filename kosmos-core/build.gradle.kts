plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "2.2.0"
}
group = "org.vorpal.kosmos"
version = "1.0-SNAPSHOT"

val serializationVersion = extra["serializationVersion"]

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")
}