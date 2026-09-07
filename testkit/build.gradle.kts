val versionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")
plugins {
    alias(libs.plugins.jvm)
}

group = "com.muedsa.snapshot"
version = "0.0.0-SNAPSHOT"

dependencies {
    implementation(project(":core"))
}

val jarBaseName = "${rootProject.name}-${project.name}"
val manifestAttributes = mapOf(
    "Implementation-Title" to jarBaseName,
    "Implementation-Version" to project.version
)

tasks.jar {
    archiveBaseName = jarBaseName
    manifest {
        attributes(manifestAttributes)
    }
}
