plugins {
    java
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.17"
    id("maven-publish")
}

val defaultJavaVersion = 23

repositories {
    mavenCentral()

    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc"
    }

    maven("https://jitpack.io") {
        name = "jitpack"
    }

    // for CoreProtectAPI
    maven("https://maven.playpro.com/") {
        name = "playpro-repo"
    }

    // worldguard
    maven("https://maven.enginehub.org/repo/") {
        name = "sk89q-repo"
    }

    maven("https://repo.codemc.org/repository/maven-public/") {
        name = "codemc"
    }
}

dependencies {
    paperweight.paperDevBundle("1.21.11-R0.1-SNAPSHOT")
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly("dev.jorel:commandapi-paper-core:11.0.0")
    compileOnly("com.github.modoruru:hitori:${properties.getOrDefault("hitori_version", "")}")

    compileOnly("net.coreprotect:coreprotect:23.0")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.16-SNAPSHOT")
    compileOnly("net.skinsrestorer:skinsrestorer-api:15.10.0")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = rootProject.name
            version = rootProject.version.toString()

            artifact(tasks.named("jar"))
        }
    }
}