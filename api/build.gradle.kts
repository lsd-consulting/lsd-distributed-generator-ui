plugins {
    kotlin("jvm")
    `maven-publish`
    id("java-library")
    id("signing")
    id("kotlin-kapt")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = "$group"
            artifactId = "${rootProject.name}-${project.name}"
            version = rootProject.version.toString()

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
                    url = uri("https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/")
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
