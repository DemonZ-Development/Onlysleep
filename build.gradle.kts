plugins {
    id("java-library")
    id("com.gradleup.shadow") version "8.3.5"
}

version = "1.2.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}


dependencies {
    // Paper API (covers Bukkit, Spigot, and Folia APIs)
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")

    // bStats Metrics (shaded into final jar)
    implementation("org.bstats:bstats-bukkit:3.1.0")

    // PlaceholderAPI (soft dependency - provided at runtime)
    compileOnly("me.clip:placeholderapi:2.11.6")

    // ---- Test dependencies ----

    // Paper API also needed for test compilation
    testImplementation("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")

    // PlaceholderAPI also needed for test compilation
    testImplementation("me.clip:placeholderapi:2.11.6")

    // JUnit 5 (Jupiter)
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")

    // Mockito for mocking Bukkit/Paper APIs
    testImplementation("org.mockito:mockito-core:5.14.2")
    testImplementation("org.mockito:mockito-junit-jupiter:5.14.2")

    // MockBukkit can be added here when a compatible version is available on Maven Central.
    // The current version requires Java 25 to build from source on JitPack (not available).
    // See: https://github.com/MockBukkit/MockBukkit
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
    withSourcesJar()
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

        // Relocate bStats to avoid conflicts with other plugins
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
