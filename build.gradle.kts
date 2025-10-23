import org.gradle.api.JavaVersion.VERSION_17
import org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.2.20"
    kotlin("plugin.spring") version "2.2.20"
    id("java-library")
    id("io.spring.dependency-management") version "1.1.7"
    java
    `maven-publish`
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
    id("com.palantir.git-version") version "3.4.0"
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

    extra["springCloudVersion"] = "2023.0.6"

    tasks.withType<KotlinCompile> {
        compilerOptions {
            freeCompilerArgs.set(listOf("-Xjsr305=strict"))
            jvmTarget.set(JVM_17)
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
