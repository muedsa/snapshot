plugins {
    alias(libs.plugins.jvm) apply false
    alias(libs.plugins.ksp) apply false
}

buildscript {
    dependencies {
        classpath(kotlin("gradle-plugin", version = "1.9.24"))
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}