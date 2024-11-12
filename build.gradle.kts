plugins {
    alias(libs.plugins.jvm) apply false
    `java-library`
}

buildscript {
    dependencies {
        classpath(kotlin("gradle-plugin", version = libs.versions.kotlin.get()))
    }
}

subprojects {
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        java {
            toolchain.languageVersion.set(JavaLanguageVersion.of(11))
        }
    }
}
