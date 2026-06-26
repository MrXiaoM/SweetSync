rootProject.name = "SweetSync"

include(":nms")
rootDir.resolve("nms").listFiles()?.forEach {
    if (it.resolve("build.gradle.kts").exists()) {
        include(":nms:${it.name}")
    }
}
