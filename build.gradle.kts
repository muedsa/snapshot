plugins {
    alias(libs.plugins.jvm) apply false
    alias(libs.plugins.ksp) apply false
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}