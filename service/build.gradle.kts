import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot") version "2.7.10"
    `maven-publish`
    id("java-library")
    id("signing")
    id("jacoco")
    id("com.palantir.git-version")
    id("kotlin-kapt")
}


//////////////////////////
// componentTest settings
//////////////////////////

sourceSets.create("componentTest") {
    withConvention(org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet::class) {
        kotlin.srcDir("src/componentTest/kotlin")
        resources.srcDir("src/componentTest/resources")
        compileClasspath += sourceSets["main"].output
        runtimeClasspath += output + compileClasspath
    }
}

val componentTest = task<Test>("componentTest") {
    description = "Runs the component tests"
    group = "verification"
    testClassesDirs = sourceSets["componentTest"].output.classesDirs
    classpath = sourceSets["componentTest"].runtimeClasspath
    testLogging.showStandardStreams = true
    useJUnitPlatform()
    mustRunAfter(tasks["test"])
    finalizedBy(tasks.jacocoTestReport)
}

val componentTestImplementation: Configuration by configurations.getting {
    extendsFrom(configurations.implementation.get())
}
val componentTestRuntimeOnly: Configuration by configurations.getting {
    extendsFrom(configurations.runtimeOnly.get())
}

configurations["componentTestImplementation"].extendsFrom(configurations.runtimeOnly.get())

tasks.check { dependsOn(componentTest) }

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
    kapt("io.github.lsd-consulting:spring-wiremock-stub-generator:2.1.2") {
        because("we want to generate WireMock stubs for client")
    }
    compileOnly("io.github.lsd-consulting:spring-wiremock-stub-generator:2.1.2")
    compileOnly("com.github.tomakehurst:wiremock-jre8:2.35.0")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.2")
    implementation("org.apache.commons:commons-collections4:4.4")
    implementation("org.apache.httpcomponents:httpcore:4.4.15") {
        because("it's needed for DB connection security")
    }

    // LSD
    implementation("io.github.lsd-consulting:lsd-distributed-generator:2.1.2")
    implementation("io.github.lsd-consulting:lsd-distributed-mongodb-access:3.0.4")

    //////////////////////////////////
    // Unit test dependencies
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2") {
        because("we want to use JUnit 5")
    }
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.2")
    testImplementation("org.junit.platform:junit-platform-commons:1.9.2")

    testImplementation("io.mockk:mockk:1.13.4") {
        because("we want to mock objects")
    }

    testImplementation("com.natpryce:hamkrest:1.8.0.1") {
        because("we want to assert nicely")
    }
    testImplementation("org.apache.commons:commons-lang3:3.12.0")
    testImplementation("org.jeasy:easy-random-core:5.0.0")

    //////////////////////////////////
    // Component test dependencies
    componentTestImplementation("org.springframework.boot:spring-boot-starter-test")

    componentTestImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2") {
        because("we want to use JUnit 5")
    }
    componentTestImplementation("de.flapdoodle.embed:de.flapdoodle.embed.mongo:3.5.4") {
        because("we want to run tests against a database")
    }
    componentTestImplementation("com.approvaltests:approvaltests:18.5.0")
    componentTestImplementation("org.jeasy:easy-random-core:5.0.0")
    componentTestImplementation("com.natpryce:hamkrest:1.8.0.1") {
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
