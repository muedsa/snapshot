val osName: String = System.getProperty("os.name")
val targetOs = when {
    osName == "Mac OS X" -> "macos"
    osName.startsWith("Win") -> "windows"
    osName.startsWith("Linux") -> "linux"
    else -> error("Unsupported OS: $osName")
}
var targetArch = when (val osArch: String = System.getProperty("os.arch")) {
    "x86_64", "amd64" -> "x64"
    "aarch64" -> "arm64"
    else -> error("Unsupported arch: $osArch")
}

group = "com.muedsa.snapshot"
version = "0.0.0-SNAPSHOT"

plugins {
    alias(libs.plugins.jvm)
    alias(libs.plugins.ksp)
}

val versionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")
dependencies {
    // Use the Kotlin JUnit 5 integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    // Use the JUnit 5 integration.
    testImplementation(libs.junit.jupiter.engine)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    api(libs.skiko.awt)
    testImplementation(versionCatalog.findLibrary("skiko-$targetOs-$targetArch").get())

    implementation(project(":processor"))
    ksp(project(":processor"))
}

tasks.test {
    useJUnitPlatform()
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

tasks.kotlinSourcesJar {
    archiveBaseName = jarBaseName
    archiveClassifier = "sources"
    manifest {
        attributes(manifestAttributes)
    }
}