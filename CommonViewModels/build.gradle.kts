plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.ktlint)
}

kotlin {
    androidTarget()

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "CommonViewModels"
            isStatic = true
        }
    }

    js(IR) {
        browser {
            binaries.executable()
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(kotlin("stdlib"))
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            implementation(project(":Storage"))
            implementation(project(":Network"))
            implementation(project(":Utils"))
            implementation(project(":Repositories"))
            implementation(libs.koin.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${libs.versions.kotlinx.coroutines.get()}")
            implementation(project(":Network"))
            implementation("io.ktor:ktor-http:${libs.versions.ktor.get()}")
        }
    }
}

android {
    namespace = "my.drivebit.viewmodels"
    compileSdk =
        libs.versions.android.compileSdk
            .get()
            .toInt()
    defaultConfig {
        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

// Генерация констант для путей к изображениям
tasks.register("generateImageConstants") {
    val resourcesDir = file("../composeApp/src/commonMain/resources/images")
    val outputDir = file("src/commonMain/kotlin/my/drivebit/resources")
    val outputFile = File(outputDir, "ImagePaths.kt")

    inputs.dir(resourcesDir)
    outputs.file(outputFile)

    doLast {
        outputDir.mkdirs()
        val constants =
            buildString {
                appendLine("package my.drivebit.resources")
                appendLine()
                appendLine("object ImagePaths {")
                resourcesDir
                    .walkTopDown()
                    .filter { it.isFile && it.extension in listOf("svg", "jpg", "png", "webp", "jpeg") }
                    .sortedBy { it.path }
                    .forEach { file ->
                        val relativePath = file.relativeTo(resourcesDir)
                        val constantName =
                            relativePath
                                .toString()
                                .replace("/", "_")
                                .replace("-", "_")
                                .replace(".", "_")
                                .uppercase()
                        val path = "images/$relativePath"
                        appendLine("    const val $constantName = \"$path\"")
                    }
                appendLine("}")
            }
        outputFile.writeText(constants)
    }
}

// Автоматически генерировать константы перед компиляцией
tasks.named("compileKotlinMetadata").configure {
    dependsOn("generateImageConstants")
}

ktlint {
    android.set(true)
    ignoreFailures.set(true)
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
    }
    filter {
        exclude("**/generated/**")
        exclude("**/build/**")
    }
}
