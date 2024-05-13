plugins {
    alias(libs.plugins.jvm) apply false
    alias(libs.plugins.ksp) apply false
    `java-library`
}

buildscript {
    dependencies {
        classpath(kotlin("gradle-plugin", version = libs.versions.kotlin.get()))
    }
}

subprojects {
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
        java {
            toolchain.languageVersion.set(JavaLanguageVersion.of(11))
        }
    }
}
