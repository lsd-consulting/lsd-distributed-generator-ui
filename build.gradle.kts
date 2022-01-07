import org.gradle.api.JavaVersion.VERSION_11
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.0"
    kotlin("plugin.spring") version "1.6.0"
    id("java-library")
    id("io.spring.dependency-management") version "1.0.9.RELEASE"
    java
    `maven-publish`
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
    id("com.palantir.git-version") version "0.12.3"
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

group = "io.github.lsd-consulting"
version = gitVersion().replaceAll("^v", "")

rootProject.version = scmVersion.version
println("Build Version = ${project.version}")

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

allprojects {
    group = "io.github.lsd-consulting"
    version = rootProject.version

    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "java")

    repositories {
        mavenLocal()
        mavenCentral()
    }

    extra["springCloudVersion"] = "2020.0.4"

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "11"
        }
    }

    java.sourceCompatibility = VERSION_11
    java.targetCompatibility = VERSION_11
    java.withJavadocJar()
    java.withSourcesJar()

    dependencyManagement {
        imports {
            mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
        }
    }
}
