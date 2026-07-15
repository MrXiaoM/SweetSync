import top.mrxiaom.gradle.LibraryHelper

plugins {
    java
    `maven-publish`
    id ("com.gradleup.shadow") version "9.3.0"
    id ("com.github.gmazzo.buildconfig") version "5.6.7"
}

buildscript {
    repositories.mavenCentral()
    dependencies.classpath("top.mrxiaom:LibrariesResolver-Gradle:1.7.28")
}
val base = LibraryHelper(project)

println("Group:   $group")
println("Version: $version")

val targetJavaVersion = 8
val pluginBaseModules = base.modules.run { listOf(library, actions, gui, paper, l10n) }
val shadowGroup = "top.mrxiaom.sweet.sync.libs"
val shadowLink = configurations.create("shadowLink")

repositories {
    mavenCentral()
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.helpch.at/releases/")
    maven("https://jitpack.io")
    maven("https://repo.rosewooddev.io/repository/public/")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.21.2-R0.1-SNAPSHOT")
    // compileOnly("org.spigotmc:spigot:1.21.2") // NMS
    compileOnly(base.depend.annotations)

    compileOnly("me.clip:placeholderapi:2.12.2")

    base.library(LibraryHelper.adventure("4.25.0"))
    base.collectPluginHolders()

    implementation(base.depend.HikariCP) { isTransitive = false }
    implementation("com.github.technicallycoded:FoliaLib:0.4.4") { isTransitive = false }
    for (artifact in pluginBaseModules) {
        implementation("$artifact")
    }
    implementation(base.resolver.lite)
    implementation(project(":nms"))
    for (item in project.project(":nms").subprojects) {
        if (item.name == "shared") {
            implementation(item)
        } else {
            add(shadowLink.name, item)
        }
    }
}
buildConfig {
    className("BuildConstants")
    packageName("top.mrxiaom.sweet.sync")

    base.doResolveLibraries()
    buildConfigField("String", "VERSION", "\"${project.version}\"")
    buildConfigField("String[]", "RESOLVED_LIBRARIES", base.join())
}

LibraryHelper.initJava(project, base, targetJavaVersion, true)
LibraryHelper.initPublishing(project)

tasks {
    shadowJar {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        configurations.add(shadowLink)
        configurations.add(project.configurations.runtimeClasspath.get())
        mapOf(
            "top.mrxiaom.pluginbase" to "base",
            "com.zaxxer.hikari" to "hikari",
            "com.tcoded.folialib" to "folialib",
        ).forEach { (original, target) ->
            relocate(original, "$shadowGroup.$target")
        }
        append("META-INF/PluginBaseHolders")
    }
}

allprojects {
    tasks.withType<JavaCompile>().configureEach {
        options.compilerArgs.add("-Xlint:-options")
    }
}
