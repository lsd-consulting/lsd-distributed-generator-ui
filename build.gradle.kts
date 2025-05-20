import org.gradle.api.JavaVersion.VERSION_17
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.1.21"
    kotlin("plugin.spring") version "2.1.21"
    id("java-library")
    id("io.spring.dependency-management") version "1.1.7"
    java
    `maven-publish`
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
    id("com.palantir.git-version") version "3.3.0"
}

nexusPublishing.repositories.sonatype {
    nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
    snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
}

tasks.create("installGitHooks") {
    shouldRunAfter("clean")
    println("-- Configuring git to use .githooks --")
    project.exec {
        commandLine("git", "config", "core.hooksPath", ".githooks")
    }
}

val gitVersion: groovy.lang.Closure<String> by extra
version = gitVersion().replace(Regex("^v"), "")
group = "io.github.lsd-consulting"

println("Version: $version")

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

allprojects {
    group = rootProject.group
    version = rootProject.version

    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "java")

    repositories {
        mavenLocal()
        mavenCentral()
    }

    extra["springCloudVersion"] = "2023.0.0"

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "17"
        }
    }

    java.sourceCompatibility = VERSION_17
    java.targetCompatibility = VERSION_17
    java.withJavadocJar()
    java.withSourcesJar()

    dependencyManagement {
        imports {
            mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
        }
    }
}
