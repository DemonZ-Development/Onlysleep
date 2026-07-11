plugins {
    id("java-library")
    id("com.gradleup.shadow") version "8.3.5"
}

version = "1.3.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}


dependencies {
    compileOnly("io.papermc.paper:paper-api:26.1.2.build.74-stable")

    implementation("org.bstats:bstats-bukkit:3.1.0")

    compileOnly("me.clip:placeholderapi:2.11.6")

    testImplementation("io.papermc.paper:paper-api:26.1.2.build.74-stable")

    testImplementation("me.clip:placeholderapi:2.11.6")

    testImplementation("org.junit.jupiter:junit-jupiter:5.13.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.13.4")

    testImplementation("org.mockito:mockito-core:5.23.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.23.0")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
    withSourcesJar()
}

configurations.configureEach {
    attributes.attribute(
        org.gradle.api.attributes.java.TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE,
        25
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
