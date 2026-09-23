import org.gradle.api.JavaVersion.VERSION_21
import org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.4.20"
    kotlin("plugin.spring") version "2.4.20"
    id("java-library")
    id("io.spring.dependency-management") version "1.1.7"
    java
    `maven-publish`
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
    id("com.palantir.git-version") version "5.1.0"
}

nexusPublishing.repositories.sonatype {
    nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
    snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
}

tasks.register<Exec>("installGitHooks") {
    shouldRunAfter("clean")
    doFirst {
        println("-- Configuring git to use .githooks --")
    }
    commandLine("git", "config", "core.hooksPath", ".githooks")
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

    extra["springCloudVersion"] = "2025.1.3"

    tasks.withType<KotlinCompile> {
        compilerOptions {
            freeCompilerArgs.set(listOf("-Xjsr305=strict"))
            jvmTarget.set(JVM_21)
        }
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
        sourceCompatibility = VERSION_21
        targetCompatibility = VERSION_21
        withJavadocJar()
        withSourcesJar()
    }

    dependencyManagement {
        imports {
            mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
        }
    }
}
