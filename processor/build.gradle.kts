plugins {
    alias(libs.plugins.jvm)
}

group = "com.muedsa.snapshot"
version = "0.0.0-SNAPSHOT"

dependencies {

    implementation(libs.ksp.symbol.processing)

    // Use the Kotlin JUnit 5 integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    // Use the JUnit 5 integration.
    testImplementation(libs.junit.jupiter.engine)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}