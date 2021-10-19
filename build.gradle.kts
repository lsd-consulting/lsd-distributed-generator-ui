import org.gradle.api.JavaVersion.VERSION_11
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.21-2"
    kotlin("plugin.spring") version "1.4.21-2"
    id("java")
    id("io.spring.dependency-management") version "1.0.9.RELEASE"
    java
//    `java-library`
    `maven-publish`
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
    id("pl.allegro.tech.build.axion-release") version "1.13.4"
}

nexusPublishing.repositories.sonatype {
    nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
    snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
}

group = "io.github.lsd-consulting"
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

    extra["springCloudVersion"] = "2020.0.3"

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
