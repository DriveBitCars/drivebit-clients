fun deleteSkikoJsArtifacts(buildDir: java.io.File) {
    val outputDirs =
        listOf(
            java.io.File(buildDir, "dist/js/productionExecutable"),
            java.io.File(buildDir, "dist/js/developmentExecutable"),
            java.io.File(buildDir, "kotlin-webpack/js/productionExecutable"),
            java.io.File(buildDir, "kotlin-webpack/js/developmentExecutable"),
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
                it.name == "unpackSkikoWasmRuntime" || it.name == "processSkikoRuntimeForKWasm"
            }.configureEach {
                enabled = false
            }

            tasks.matching {
                it.name == "jsBrowserDistribution" ||
                    it.name == "jsBrowserProductionWebpack" ||
                    it.name == "jsBrowserDevelopmentWebpack" ||
                    it.name == "jsDevelopmentExecutableCompileSync" ||
                    it.name == "jsProductionExecutableCompileSync"
            }.configureEach {
                doLast {
                    deleteSkikoJsArtifacts(layout.buildDirectory.get().asFile)
                }
            }
        }
    }
}
