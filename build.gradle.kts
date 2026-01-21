plugins {
    `maven-publish`
    id("hytale-mod") version "0.+"
}

group = "dev.jojo"
version = "0.1.0"
val javaVersion = 25

repositories {
    mavenCentral()
    maven("https://maven.hytale-modding.info/releases") {
        name = "HytaleModdingReleases"
    }
}

dependencies {
    compileOnly(libs.jetbrains.annotations)
    compileOnly(libs.jspecify)
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.0")
}

hytale {
    addAssetsDependency = true
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(javaVersion)
    }

    withSourcesJar()
}

tasks.named<ProcessResources>("processResources") {
    var replaceProperties = mapOf(
        "plugin_group" to findProperty("plugin_group"),
        "plugin_maven_group" to project.group,
        "plugin_name" to project.name,
        "plugin_version" to project.version,
        "server_version" to findProperty("server_version"),
        "plugin_description" to findProperty("plugin_description"),
        "plugin_website" to findProperty("plugin_website"),
        "plugin_main_entrypoint" to findProperty("plugin_main_entrypoint"),
        "plugin_author" to findProperty("plugin_author")
    )

    filesMatching("manifest.json") {
        expand(replaceProperties)
    }

    inputs.properties(replaceProperties)
}

tasks.withType<Jar> {
    manifest {
        attributes["Specification-Title"] = rootProject.name
        attributes["Specification-Version"] = version
        attributes["Implementation-Title"] = project.name
        attributes["Implementation-Version"] =
            providers.environmentVariable("COMMIT_SHA_SHORT")
                .map { "${version}-${it}" }
                .getOrElse(version.toString())
    }
}

// Fat JAR - déclaré dans afterEvaluate pour éviter les problèmes de résolution
afterEvaluate {
    tasks.register<Jar>("fatJar") {
        archiveClassifier.set("")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
        from(sourceSets.main.get().output)

        manifest {
            attributes["Specification-Title"] = rootProject.name
            attributes["Specification-Version"] = version
            attributes["Implementation-Title"] = project.name
            attributes["Implementation-Version"] = version.toString()
        }
    }

    tasks.build {
        dependsOn("fatJar")
    }
}

publishing {
    repositories {}
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}

val syncAssets = tasks.register<Copy>("syncAssets") {
    group = "hytale"
    description = "Automatically syncs assets from Build back to Source after server stops."
    from(layout.buildDirectory.dir("resources/main"))
    into("src/main/resources")
    exclude("manifest.json")
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    doLast {
        println("✅ Assets successfully synced from Game to Source Code!")
    }
}

afterEvaluate {
    val targetTask = tasks.findByName("runServer") ?: tasks.findByName("server")
    if (targetTask != null) {
        targetTask.finalizedBy(syncAssets)
        logger.lifecycle("✅ specific task '${targetTask.name}' hooked for auto-sync.")
    } else {
        logger.warn("⚠️ Could not find 'runServer' or 'server' task to hook auto-sync into.")
    }
}