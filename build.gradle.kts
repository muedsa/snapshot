import org.gradle.api.plugins.BasePluginExtension
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar

plugins {
    base
    alias(libs.plugins.jvm) apply false
}

buildscript {
    dependencies {
        classpath(kotlin("gradle-plugin", version = libs.versions.kotlin.get()))
    }
}

subprojects {
    group = "com.muedsa.snapshot"
    version = "0.0.0-SNAPSHOT"

    pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
        extensions.configure<JavaPluginExtension> {
            toolchain.languageVersion.set(JavaLanguageVersion.of(11))
        }
        extensions.configure<BasePluginExtension> {
            archivesName.set("${rootProject.name}-${project.name}")
        }
        tasks.withType<Jar>().configureEach {
            manifest {
                attributes(
                    "Implementation-Title" to "${rootProject.name}-${project.name}",
                    "Implementation-Version" to project.version,
                )
                if (project.name == "core" || project.name == "parser") {
                    attributes("Automatic-Module-Name" to "${project.group}.${project.name}")
                }
            }
        }
    }
}

tasks.register("releaseJars") {
    group = "build"
    description = "构建可交付的 core/parser 二进制与源码 JAR"
    dependsOn(
        ":core:jar",
        ":core:kotlinSourcesJar",
        ":parser:jar",
        ":parser:kotlinSourcesJar",
    )
}

configure(listOf(project(":core"), project(":parser"))) {
    pluginManager.apply("maven-publish")
    pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
        val moduleName = project.name
        extensions.configure<PublishingExtension> {
            publications {
                create<MavenPublication>("githubPackages") {
                    from(components["java"])
                    artifact(tasks.named("kotlinSourcesJar"))
                    artifactId = "${rootProject.name}-$moduleName"
                    pom {
                        name.set("Snapshot $moduleName")
                        description.set(
                            if (moduleName == "core") {
                                "Kotlin/Skia structured image rendering core"
                            } else {
                                "DOM-like text parser for Snapshot"
                            },
                        )
                        url.set("https://github.com/muedsa/snapshot")
                        licenses {
                            license {
                                name.set("MIT License")
                                url.set("https://opensource.org/licenses/MIT")
                            }
                        }
                        scm {
                            connection.set("scm:git:https://github.com/muedsa/snapshot.git")
                            url.set("https://github.com/muedsa/snapshot")
                        }
                    }
                }
            }
            repositories {
                maven {
                    name = "GitHubPackages"
                    url = uri("https://maven.pkg.github.com/muedsa/snapshot")
                    credentials {
                        username = providers.environmentVariable("GITHUB_ACTOR").orNull
                        password = providers.environmentVariable("GITHUB_TOKEN").orNull
                    }
                }
            }
        }
    }
}
