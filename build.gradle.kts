plugins {
    id("java-library")
    id("com.gradleup.shadow") version "9.5.0"
}

version = "1.3.0"

val paperApiVersion = providers.gradleProperty("paperApiVersion")
    .orElse("1.20.4-R0.1-SNAPSHOT")
val paperApiJvmVersion = providers.gradleProperty("paperApiJvmVersion")
    .map(String::toInt)
    .orElse(21)

repositories {
    mavenCentral()
    if (paperApiVersion.get() == "1.20.5-R0.1-SNAPSHOT") {
        exclusiveContent {
            forRepository {
                maven("https://repo.papermc.io/repository/maven-public/") {
                    name = "paperApiArchive"
                    metadataSources {
                        gradleMetadata()
                        artifact()
                    }
                }
            }
            filter {
                includeModule("io.papermc.paper", "paper-api")
            }
        }
    }
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}


dependencies {
    compileOnly("io.papermc.paper:paper-api:${paperApiVersion.get()}")

    implementation("org.bstats:bstats-bukkit:3.1.0")

    compileOnly("me.clip:placeholderapi:2.11.6")

    testImplementation("io.papermc.paper:paper-api:${paperApiVersion.get()}")

    testImplementation("me.clip:placeholderapi:2.11.6")

    testImplementation("org.junit.jupiter:junit-jupiter:5.13.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.13.4")

    testImplementation("org.mockito:mockito-core:5.23.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.23.0")

    components {
        withModule("io.papermc.paper:paper-api") {
            allVariants {
                withDependencies {
                    filter {
                        it.group == "net.kyori" &&
                            it.name == "adventure-bom" &&
                            it.versionConstraint.requiredVersion == "4.17.0-SNAPSHOT"
                    }.forEach {
                        it.version {
                            require("4.17.0")
                        }
                    }
                }
            }
        }
    }
}

java {
    withSourcesJar()
}

configurations.configureEach {
    attributes.attribute(
        org.gradle.api.attributes.java.TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE,
        paperApiJvmVersion.get()
    )
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
        options.release = 21
    }

    compileTestJava {
        options.encoding = "UTF-8"
        options.release = 21
    }

    processResources {
        filesMatching("**/*.yml") {
            expand("project" to mapOf("version" to version))
        }
    }

    shadowJar {
        archiveBaseName = "Onlysleep"
        archiveClassifier = ""
        archiveVersion = version as String

        relocate("org.bstats", "com.demonzdevelopment.onlysleep.libs.bstats")

        minimize {
            exclude(dependency("org.bstats:bstats-bukkit:.*"))
        }
    }

    build {
        dependsOn(shadowJar)
    }

    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
            showExceptions = true
            showCauses = true
            showStackTraces = true
        }
    }
}
