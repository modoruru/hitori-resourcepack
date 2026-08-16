plugins {
    java
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21"
    id("maven-publish")
    id("com.gradleup.shadow") version "9.4.3"
}

val defaultJavaVersion = "25"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of((property("java") ?: defaultJavaVersion) as String))
    }
}

repositories {
    mavenCentral()

    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc"
    }

    maven("https://jitpack.io") {
        name = "jitpack"
    }

    maven("https://repository.modoru.fun/releases") {
        name = "modoruReleases"
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
    paperweight.paperDevBundle("26.2.build.+")
    compileOnly("io.papermc.paper:paper-api:26.2.build.+")

    compileOnly("su.hitori:hitori:${property("hitori_version")}")
    compileOnly("net.coreprotect:coreprotect:23.2")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.19-SNAPSHOT")
    compileOnly("net.skinsrestorer:skinsrestorer-api:15.10.0")

    implementation("com.github.justlofe:FastBytes:${property("fastbytes_version")}")
}

tasks {
    jar {
        enabled = false
    }

    shadowJar {
        archiveClassifier.set("")
    }

    build {
        dependsOn(shadowJar)
    }

    processResources {
        filesMatching("hitori.properties") {
            expand(mapOf("version" to project.version))
        }
    }
}

publishing {
    repositories {
        maven {
            name = "modoruReleases"
            url = uri("https://repository.modoru.fun/releases")

            credentials {
                username = System.getenv("MODORU_USERNAME") ?: ""
                password = System.getenv("MODORU_TOKEN") ?: ""
            }
        }
    }

    publications {
        create<MavenPublication>("maven") {
            artifactId = "hitori-resourcepack"
            group = "su.hitori"
            version = rootProject.version.toString()

            artifact(tasks.named("shadowJar"))
        }
    }
}