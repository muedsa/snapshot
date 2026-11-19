pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version("1.0.0")
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url = uri("https://redirector.kotlinlang.org/maven/compose-dev") }
    }
}
rootProject.name = "snapshot"
include(":core")
include(":parser")
include(":testkit")
