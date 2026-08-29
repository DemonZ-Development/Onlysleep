plugins {
    id("net.fabricmc.fabric-loom") version "1.18.0-alpha.17"
}

version = "1.4.0"
base.archivesName = "Onlysleep-Fabric"

val minecraftVersion = "26.2"
val fabricApiVersion = "0.158.0+26.2"

dependencies {
    minecraft("com.mojang:minecraft:${minecraftVersion}")
    implementation("net.fabricmc:fabric-loader:0.19.3")
    implementation("net.fabricmc.fabric-api:fabric-api:${fabricApiVersion}")

    implementation("me.lucko:fabric-permissions-api:0.7.0")
    include("me.lucko:fabric-permissions-api:0.7.0")

    testImplementation("org.junit.jupiter:junit-jupiter:5.13.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.13.4")
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

tasks {
    processResources {
        filesMatching("fabric.mod.json") {
            expand("version" to version)
        }
    }

    compileJava {
        options.encoding = "UTF-8"
        options.release = 25
    }

    compileTestJava {
        options.encoding = "UTF-8"
        options.release = 25
    }

    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
}
