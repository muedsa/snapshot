plugins {
    alias(libs.plugins.jvm)
}

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

val versionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")
dependencies {
    // 本模块自带测试:锁定采样/区域断言的边界语义(见 SamplingAssertionsBoundaryTest)
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation(libs.junit.jupiter.engine)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    // 测试要构造 Pixmap,需平台相关的 skiko 原生库
    testImplementation(versionCatalog.findLibrary("skiko-$targetOs-$targetArch").get())

    implementation(project(":core"))
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
