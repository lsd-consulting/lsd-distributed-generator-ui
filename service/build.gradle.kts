import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot") version "3.4.4"
    `maven-publish`
    id("java-library")
    id("signing")
    id("jacoco")
    id("com.palantir.git-version")
    id("kotlin-kapt")
}

tasks.withType<Copy>().all { duplicatesStrategy = DuplicatesStrategy.EXCLUDE }

//////////////////////////
// mongoComponentTest settings
//////////////////////////

sourceSets.create("mongoComponentTest") {
    withConvention(org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet::class) {
        kotlin.srcDir("src/mongoComponentTest/kotlin")
        resources.srcDir("src/mongoComponentTest/resources")
        compileClasspath += sourceSets["main"].output
        runtimeClasspath += output + compileClasspath
    }
}

val mongoComponentTest = task<Test>("mongoComponentTest") {
    description = "Runs the component tests"
    group = "verification"
    testClassesDirs = sourceSets["mongoComponentTest"].output.classesDirs
    classpath = sourceSets["mongoComponentTest"].runtimeClasspath
    testLogging.showStandardStreams = true
    useJUnitPlatform()
    mustRunAfter(tasks["test"])
    finalizedBy(tasks.jacocoTestReport)
}

val mongoComponentTestImplementation: Configuration by configurations.getting {
    extendsFrom(configurations.implementation.get())
}
val mongoComponentTestRuntimeOnly: Configuration by configurations.getting {
    extendsFrom(configurations.runtimeOnly.get())
}

configurations["mongoComponentTestImplementation"].extendsFrom(configurations.runtimeOnly.get())

tasks.check { dependsOn(mongoComponentTest) }

//////////////////////////
// postgresComponentTest settings
//////////////////////////

sourceSets.create("postgresComponentTest") {
    withConvention(org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet::class) {
        kotlin.srcDir("src/postgresComponentTest/kotlin")
        resources.srcDir("src/postgresComponentTest/resources")
        compileClasspath += sourceSets["main"].output
        runtimeClasspath += output + compileClasspath
    }
}

val postgresComponentTest = task<Test>("postgresComponentTest") {
    description = "Runs the PostgreSQL component tests"
    group = "verification"
    testClassesDirs = sourceSets["postgresComponentTest"].output.classesDirs
    classpath = sourceSets["postgresComponentTest"].runtimeClasspath
    testLogging.showStandardStreams = true
    useJUnitPlatform()
    mustRunAfter(tasks["mongoComponentTest"])
    finalizedBy(tasks.jacocoTestReport)
}

val postgresComponentTestImplementation: Configuration by configurations.getting {
    extendsFrom(configurations.implementation.get())
}
val postgresComponentTestRuntimeOnly: Configuration by configurations.getting {
    extendsFrom(configurations.runtimeOnly.get())
}

configurations["postgresComponentTestImplementation"].extendsFrom(configurations.runtimeOnly.get())

tasks.check { dependsOn(postgresComponentTest) }

//////////////////////////
// unit test settings
//////////////////////////

tasks {
    test {
        useJUnitPlatform()
    }
}

//////////////////////////
// dependencies
//////////////////////////

dependencies {
    api(project(":api"))

    // Spring
    implementation("org.springframework.boot:spring-boot-starter-data-rest")

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // WireMockStubGenerator
    kapt("io.github.lsd-consulting:spring-wiremock-stub-generator:3.1.23") {
        because("we want to generate WireMock stubs for client")
    }
    compileOnly("io.github.lsd-consulting:spring-wiremock-stub-generator:3.1.23")
    compileOnly("org.wiremock:wiremock-standalone:3.12.1")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.3")
    implementation("org.apache.commons:commons-collections4:4.4")
    implementation("org.apache.httpcomponents:httpcore:4.4.16") {
        because("it's needed for DB connection security")
    }

    // LSD
    implementation("io.github.lsd-consulting:lsd-distributed-generator:8.2.38")

    //////////////////////////////////
    // Unit test dependencies
    testImplementation("org.junit.jupiter:junit-jupiter-api") {
        because("we want to use JUnit 5")
    }
    testImplementation("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.junit.platform:junit-platform-commons")

    testImplementation("io.mockk:mockk:1.14.0") {
        because("we want to mock objects")
    }

    testImplementation("com.natpryce:hamkrest:1.8.0.1") {
        because("we want to assert nicely")
    }
    testImplementation("org.apache.commons:commons-lang3:3.17.0")
    testImplementation("org.jeasy:easy-random-core:5.0.0")

    //////////////////////////////////
    // Component test dependencies
    mongoComponentTestImplementation("org.springframework.boot:spring-boot-starter-test")

    mongoComponentTestImplementation("io.github.lsd-consulting:lsd-distributed-mongodb-connector:6.1.10")
    mongoComponentTestImplementation("org.mongodb:mongodb-driver-core:5.4.0")
    mongoComponentTestImplementation("org.mongodb:mongodb-driver-sync:5.4.0")

    mongoComponentTestImplementation("org.junit.jupiter:junit-jupiter-engine")
    mongoComponentTestImplementation("org.junit.jupiter:junit-jupiter-api") {
        because("we want to use JUnit 5")
    }
    mongoComponentTestImplementation("org.testcontainers:junit-jupiter:1.20.6")
    mongoComponentTestImplementation("org.testcontainers:mongodb:1.20.6") {
        because("we want to test against a real MongoDb in Docker")
    }
    mongoComponentTestImplementation("com.approvaltests:approvaltests:24.20.0")
    mongoComponentTestImplementation("org.jeasy:easy-random-core:5.0.0")
    mongoComponentTestImplementation("com.natpryce:hamkrest:1.8.0.1") {
        because("we want to assert nicely")
    }

    //////////////////////////////////
    // PostgreSQL component test dependencies
    postgresComponentTestImplementation("org.springframework.boot:spring-boot-starter-test")

    postgresComponentTestImplementation("io.github.lsd-consulting:lsd-distributed-postgres-connector:3.2.0")
    postgresComponentTestImplementation("com.zaxxer:HikariCP:6.3.0")

    postgresComponentTestImplementation("org.junit.jupiter:junit-jupiter-engine")
    postgresComponentTestImplementation("org.junit.jupiter:junit-jupiter-api") {
        because("we want to use JUnit 5")
    }
    postgresComponentTestImplementation("org.testcontainers:postgresql:1.20.6")
    postgresComponentTestImplementation("org.testcontainers:junit-jupiter:1.20.6")
    postgresComponentTestImplementation("org.testcontainers:postgresql:1.20.6")

    postgresComponentTestImplementation("com.approvaltests:approvaltests:24.20.0")
    postgresComponentTestImplementation("com.natpryce:hamkrest:1.8.0.1") {
        because("we want to assert nicely")
    }
}

//////////////////////////
// WireMockStubGenerator
//////////////////////////

val compileJava = project.tasks.named("compileJava").get() as JavaCompile
tasks.register<JavaCompile>("compileStubs") {
    classpath = compileJava.classpath
    source = project.layout.buildDirectory.dir("generated-stub-sources").get().asFileTree
    val stubsClassesDir = project.buildDir.resolve("generated-stub-classes")
    destinationDirectory.set(stubsClassesDir)
}
compileJava.finalizedBy(tasks.getByName("compileStubs"))

val compileJavaStubs = project.tasks.named("compileStubs").get() as JavaCompile
tasks.register<Jar>("stubsJar") {
    description = "Java Wiremock stubs JAR"
    group = "Verification"
    archiveBaseName.set(project.provider { project.name })
    archiveClassifier.set("wiremock-stubs")
    from(compileJavaStubs.destinationDirectory)
    dependsOn(compileJavaStubs)
}
val stubsJar = artifacts.add("archives", tasks.getByName("stubsJar"))
compileJavaStubs.finalizedBy(tasks.getByName("stubsJar"))

//////////////////////////
// Jacoco
//////////////////////////

jacoco {
    toolVersion = "0.8.7"
}

tasks.jacocoTestReport {
    executionData(
        file("${project.buildDir}/jacoco/componentTest.exec")
    )
    reports {
        xml.required.set(true)
        html.required.set(true)
        html.setDestination(project.provider { File("${project.buildDir}/reports/coverage") })
    }
}

//////////////////////////
// publishing
//////////////////////////

tasks.getByName<BootJar>("bootJar") {
    enabled = true
    archiveClassifier.set("boot")
}

tasks.getByName<Jar>("jar") {
    enabled = true
    archiveClassifier.set("")
}

project.tasks.publish {
    dependsOn(project.tasks.bootJar)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = "$group"
            artifactId = "${rootProject.name}-${project.name}"
            version = rootProject.version.toString()

            artifact(project.tasks.bootJar)
            artifact(stubsJar)

            from(components["java"])
            pom {
                name.set("lsd-distributed-generator-ui")
                description.set("This is the graphical interface providing the LSD generation functionality.")
                url.set("https://github.com/lsd-consulting/lsd-distributed-generator-ui")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://github.com/lsd-consulting/lsd-distributed-generator-ui/blob/main/LICENSE.txt")
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        name.set("Lukasz")
                        email.set("lukasz.gryzbon@gmail.com")
                        organization.set("Integreety Ltd.")
                        organizationUrl.set("https://www.integreety.co.uk")
                    }
                    developer {
                        name.set("Nick")
                        email.set("nicholas.mcdowall@gmail.com")
                        organization.set("NKM IT Solutions")
                        organizationUrl.set("https://github.com/nickmcdowall")
                    }
                }
                scm {
                    url.set("https://github.com/lsd-consulting/lsd-distributed-generator-ui.git")
                }
            }
            repositories {
                maven {
                    name = "sonatype"
                    url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                    credentials(PasswordCredentials::class)
                }
            }
        }
    }
}

signing {
    project.findProperty("signingKey")?.let {
        // Use in-memory ascii-armored keys
        val signingKey: String? by project
        val signingPassword: String? by project
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications["mavenJava"])
    } ?: run {
        sign(publishing.publications["mavenJava"])
    }
}
