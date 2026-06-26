plugins {
    id("java")
}

fun Project.setupJava(targetJavaVersion: Int) {
    project.extensions.configure<JavaPluginExtension> {
        disableAutoTargetJvm()
    }
    project.tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release = targetJavaVersion
    }
}

allprojects {
    apply(plugin="java")
    repositories {
        mavenCentral()
        maven("https://repo.codemc.io/repository/maven-public/")
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://repo.rosewooddev.io/repository/public/")
        maven("https://repo.codemc.io/repository/nms/")
        maven("https://libraries.minecraft.net/")
    }

    dependencies {
        compileOnly("org.jetbrains:annotations:24.0.0")
    }
}

subprojects {
    val targetJavaVersion = property("target-java-version").toString().toInt()
    setupJava(targetJavaVersion)

    dependencies {
        if (project.name != "shared") {
            add("compileOnly", project(":nms:shared"))
        }
    }
}

setupJava(8)
dependencies {
    for (item in subprojects) {
        add("compileOnly", item)
    }
}
