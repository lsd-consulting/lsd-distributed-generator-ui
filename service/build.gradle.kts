import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot") version "2.5.5"
    `maven-publish`
    id("java-library")
    id("signing")
    id("pl.allegro.tech.build.axion-release")
}

//////////////////////////
// componentTest source set
//////////////////////////

sourceSets.create("componentTest") {
    withConvention(org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet::class) {
        kotlin.srcDir("src/componentTest/kotlin")
        resources.srcDir("src/componentTest/resources")
        compileClasspath += sourceSets["main"].output + configurations["testRuntimeClasspath"]
        runtimeClasspath += output + compileClasspath + sourceSets["test"].runtimeClasspath
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
}

val componentTestImplementation: Configuration by configurations.getting {
    extendsFrom(configurations.testImplementation.get())
}
val componentTestRuntimeOnly: Configuration by configurations.getting {
    extendsFrom(configurations.testRuntimeOnly.get())
}

configurations["componentTestImplementation"].extendsFrom(configurations.runtimeOnly.get())

tasks.check { dependsOn(componentTest) }

//////////////////////////
// dependencies
//////////////////////////

dependencies {
    // Spring
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("org.springframework.boot:spring-boot-starter-data-rest")
    implementation("org.springframework.cloud:spring-cloud-starter-sleuth")

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.0")
    implementation("org.apache.commons:commons-collections4:4.4")
    implementation("io.pebbletemplates:pebble:3.1.5")
    implementation("org.apache.httpcomponents:httpcore:4.4.14") {
        because("it's needed for DB connection security")
    }

    // LSD
    implementation("io.github.lsd-consulting:lsd-core:0.1.32")
    implementation("io.github.lsd-consulting:lsd-distributed-generator:0.1.5")

    //////////////////////////////////
    // Unit test dependencies
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1") {
        because("we want to use JUnit 5")
    }

    //////////////////////////////////
    // Component test dependencies
    componentTestImplementation("org.springframework.boot:spring-boot-starter-test")

    componentTestImplementation("io.github.lsd-consulting:lsd-cucumber:0.1.1") {
        // TODO Probably should be removed
        because("we want to include the Cucumber scenarios in the LSDs")
    }

    // JUnit 5
    componentTestImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1") {
        because("we want to use JUnit 5")
    }
    componentTestImplementation("org.junit.jupiter:junit-jupiter-params:5.8.1") {
        because("we want to run parameterised tests")
    }

    // Cucumber
    componentTestImplementation("io.cucumber:cucumber-java8:6.11.0") {
        because("we want to use Cucumber JVM")
    }
    componentTestImplementation("io.cucumber:cucumber-junit-platform-engine:6.11.0") {
        because("we want to use Cucumber with JUnit 5")
    }
    componentTestImplementation("io.cucumber:cucumber-spring:6.11.0") {
        because("we want to use dependency injection in our Cucumber tests")
    }
    componentTestImplementation("de.monochromata.cucumber:reporting-plugin:4.0.103") {
        because("we want to see useful Cucumber reports")
    }

//    componentTestImplementation("com.approvaltests:approvaltests:11.7.0")
    componentTestImplementation("de.flapdoodle.embed:de.flapdoodle.embed.mongo:3.0.0")
}

tasks.getByName<BootJar>("bootJar") {
    enabled = true
    classifier = "boot"
}

tasks.getByName<Jar>("jar") {
    enabled = true
    classifier = ""
}

project.tasks.publish {
    dependsOn(project.tasks.bootJar)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = "$group"
            artifactId = "lsd-distributed-generator-ui"
            version = scmVersion.version

            artifact(project.tasks.bootJar)

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
    sign(publishing.publications["mavenJava"])
}
