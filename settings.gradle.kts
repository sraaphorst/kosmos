plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
rootProject.name = "kosmos"
include(
    ":kosmos-core",
    ":kosmos-laws",
    "kosmos-noise",
    "kosmos-testkit"
)