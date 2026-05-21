import org.jetbrains.kotlin.gradle.dsl.JvmTarget

import groovy.json.JsonSlurper

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.kotlinxSerialization)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    val iosArm64Target = iosArm64()
    val iosSimulatorArm64Target = iosSimulatorArm64()

    listOf(
        iosArm64Target,
        iosSimulatorArm64Target,
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    js(IR) {
        browser {
            commonWebpackConfig {
                outputFileName = "composeApp.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        val androidMaestro by creating {
            dependsOn(androidMain.get())
        }

        androidMaestro.kotlin.srcDir("src/androidMaestro/kotlin")
        androidMaestro.resources.srcDir("src/androidMaestro/res")

        androidMaestro.dependencies {
            implementation(project(":Mobile"))
            implementation(project(":Network"))
            implementation(project(":UI-Components"))
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(libs.voyager.navigator)
            implementation(libs.androidx.activity.compose)
        }

        androidMain.dependencies {
            implementation(project(":Splash"))
            implementation(project(":Mobile"))
            implementation(project(":Auth"))
            implementation(project(":UI-Components"))
            implementation(compose.preview)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.voyager.navigator)
            implementation(libs.voyager.screenmodel)
            implementation(libs.voyager.tabs)
            implementation(libs.voyager.koin)
            implementation(libs.koin.compose)
            implementation(libs.androidx.activity.compose)
        }

        val appleMain by creating {
            dependsOn(commonMain.get())
        }

        iosArm64Target.compilations
            .getByName("main")
            .defaultSourceSet
            .dependsOn(appleMain)
        iosSimulatorArm64Target.compilations
            .getByName("main")
            .defaultSourceSet
            .dependsOn(appleMain)

        appleMain.dependencies {
            implementation(project(":Splash"))
            implementation(project(":Mobile"))
            implementation(project(":Auth"))
            implementation(project(":UI-Components"))
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.voyager.navigator)
            implementation(libs.voyager.screenmodel)
            implementation(libs.voyager.tabs)
            implementation(libs.voyager.koin)
            implementation(libs.koin.compose)
        }

        jsMain.dependencies {
            implementation(compose.html.core)
            implementation(compose.runtime)
            implementation(libs.kotlinx.serialization.json)
            implementation(project(":Storage"))
            implementation(project(":CommonViewModels"))
            implementation(project(":Network"))
            implementation(project(":Repositories"))
            implementation(project(":UI-Components"))
            implementation(project(":Utils"))
            implementation(project(":Maps"))
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koinComposeViewmodelJs)
        }

        commonMain.dependencies {
            implementation(project(":Storage"))
            implementation(project(":CommonViewModels"))
            implementation(project(":Repositories"))
            implementation(project(":Utils"))
            implementation(compose.runtime)
            implementation(libs.koin.core)
            implementation(libs.kotlinx.datetime)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.koin.core)
            implementation(libs.koin.test)
        }
        androidUnitTest.dependencies {
            implementation(libs.kotlin.testJunit)
            implementation(libs.junit)
            implementation(libs.androidx.testExt.junit)
        }

        jsTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}

android {
    namespace = "my.drivebit.mobile"
    compileSdk =
        libs.versions.android.compileSdk
            .get()
            .toInt()

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "my.drivebit.clients"
        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()
        targetSdk =
            libs.versions.android.targetSdk
                .get()
                .toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
        create("maestro") {
            initWith(getByName("debug"))
            matchingFallbacks += listOf("debug", "release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

// ktlint configuration
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

// Изображения хранятся в composeApp/src/commonMain/resources/images/
// Для Web (JS/WASM) они автоматически копируются в productionExecutable/images/
// Никаких дополнительных задач не требуется

// Настройка обработки дубликатов для JS ресурсов
tasks.withType<org.gradle.api.tasks.Copy>().configureEach {
    duplicatesStrategy = org.gradle.api.file.DuplicatesStrategy.INCLUDE
}

tasks.register<Exec>("generateMoskvaSeoSnapshots") {
    dependsOn("syncSeoLandingHtml")
    commandLine("python3", rootProject.file("scripts/generate_moskva_seo.py").absolutePath)
    environment("DRIVEBIT_API_BASE", "http://157.22.252.70:5000")
}

tasks.register<Exec>("generateSearchBrandSeoSnapshots") {
    commandLine("python3", rootProject.file("scripts/generate_search_brand_seo.py").absolutePath)
    environment("DRIVEBIT_API_BASE", "http://157.22.252.70:5000")
}

tasks.register("generateAllSeoSnapshots") {
    dependsOn("generateMoskvaSeoSnapshots", "generateSearchBrandSeoSnapshots")
}

tasks.named<org.gradle.api.tasks.Copy>("jsProcessResources").configure {
    dependsOn("generateAllSeoSnapshots")
    from(rootProject.layout.projectDirectory.file("index.html"))
    from(rootProject.layout.projectDirectory.dir("vendor")) {
        into("vendor")
    }
    from(layout.projectDirectory.file("seo/landing-blocks.json")) {
        into("seo")
    }
}

val seoLandingHtmlTargets =
    mapOf(
        "/" to rootProject.layout.projectDirectory.file("index.html"),
        "/moskva" to layout.projectDirectory.file("src/jsMain/resources/moskva/index.html"),
        "/moskva/k-rodnym" to layout.projectDirectory.file("src/jsMain/resources/moskva/k-rodnym/index.html"),
        "/moskva/kanikuly" to layout.projectDirectory.file("src/jsMain/resources/moskva/kanikuly/index.html"),
        "/moskva/komandirovki" to layout.projectDirectory.file("src/jsMain/resources/moskva/komandirovki/index.html"),
        "/moskva/meropriyatie" to layout.projectDirectory.file("src/jsMain/resources/moskva/meropriyatie/index.html"),
        "/moskva/pereezd" to layout.projectDirectory.file("src/jsMain/resources/moskva/pereezd/index.html"),
        "/moskva/poblizosti" to layout.projectDirectory.file("src/jsMain/resources/moskva/poblizosti/index.html"),
        "/moskva/puteshestviya" to layout.projectDirectory.file("src/jsMain/resources/moskva/puteshestviya/index.html"),
        "/moskva/za-gorod" to layout.projectDirectory.file("src/jsMain/resources/moskva/za-gorod/index.html"),
    )

tasks.register("syncSeoLandingHtml") {
    val blocksFile = layout.projectDirectory.file("seo/landing-blocks.json")
    inputs.file(blocksFile)
    seoLandingHtmlTargets.values.forEach { outputs.file(it) }

    doLast {
        val blocks =
            JsonSlurper().parse(blocksFile.asFile) as Map<String, Map<String, Any>>
        seoLandingHtmlTargets.forEach { (path, htmlFile) ->
            val block =
                blocks[path]
                    ?: error("Missing SEO block for path $path in ${blocksFile.asFile}")
            val aria = block["ariaLabel"] as String
            val h2 = block["h2"] as String
            @Suppress("UNCHECKED_CAST")
            val paragraphs = block["paragraphs"] as List<String>
            val shell =
                buildString {
                    append("    <div class=\"drivebit-seo-shell\">\n")
                    append("        <section class=\"drivebit-seo-text\" aria-label=\"")
                    append(aria.replace("\"", "&quot;"))
                    append("\">\n")
                    append("            <h2>")
                    append(h2)
                    append("</h2>\n")
                    paragraphs.forEach { paragraph ->
                        append("            <p>")
                        append(paragraph)
                        append("</p>\n")
                    }
                    append("        </section>\n")
                    append("    </div>\n\n")
                }

            val file = htmlFile.asFile
            val text = file.readText(Charsets.UTF_8)
            val start = text.indexOf("<motion.div class=\"drivebit-seo-shell\">")
                .takeIf { it >= 0 }
                ?: text.indexOf("<div class=\"drivebit-seo-shell\">")
            val generatedStart = text.indexOf("<!-- drivebit-seo-generated-start -->", start)
            val footerStart = text.indexOf("<div class=\"drivebit-footer-shell\">", start)
            val replaceEnd =
                when {
                    generatedStart >= 0 -> generatedStart
                    footerStart >= 0 -> footerStart
                    else -> -1
                }
            if (start < 0 || replaceEnd < 0) {
                error("SEO shell markers not found in ${file.path}")
            }
            file.writeText(text.substring(0, start) + shell + text.substring(replaceEnd), Charsets.UTF_8)
        }
    }
}
