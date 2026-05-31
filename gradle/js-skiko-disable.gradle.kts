fun deleteSkikoJsArtifacts(buildDir: java.io.File) {
    val outputDirs =
        listOf(
            java.io.File(buildDir, "dist/js/productionExecutable"),
        )
    outputDirs.forEach { dir ->
        if (!dir.isDirectory) return@forEach
        dir.listFiles()?.forEach { file ->
            val name = file.name.lowercase()
            if (name.contains("skiko") || name == "js-reexport-symbols.mjs") {
                file.deleteRecursively()
            }
        }
    }
}

subprojects {
    plugins.withId("org.jetbrains.kotlin.multiplatform") {
        afterEvaluate {
            tasks.matching {
                it.name == "jsBrowserDistribution"
            }.configureEach {
                doLast {
                    deleteSkikoJsArtifacts(layout.buildDirectory.get().asFile)
                }
            }
        }
    }
}
