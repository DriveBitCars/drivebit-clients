import org.jetbrains.kotlin.gradle.dsl.JvmTarget

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
            implementation(project(":WebShell"))
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

val needsSplitBundleDevWebpack =
    gradle.startParameter.taskNames.any {
        it.contains("jsBrowserDevelopmentRun", ignoreCase = true) ||
            (it.contains("composeApp", ignoreCase = true) && it.contains("Development", ignoreCase = true))
    }

tasks.named<org.gradle.api.tasks.Copy>("jsProcessResources").configure {
    if (needsSplitBundleDevWebpack) {
        dependsOn(
            ":carDetailApp:jsBrowserDevelopmentWebpack",
            ":myCarsApp:jsBrowserDevelopmentWebpack",
        )
    } else {
        dependsOn(
            ":carDetailApp:jsBrowserProductionWebpack",
            ":myCarsApp:jsBrowserProductionWebpack",
        )
    }
    from(rootProject.layout.projectDirectory.file("index.html"))
    from(rootProject.layout.projectDirectory.dir("vendor")) {
        into("vendor")
    }
    from(layout.projectDirectory.file("seo/landing-blocks.json")) {
        into("seo")
    }
    from(project(":carDetailApp").layout.projectDirectory.dir("src/jsMain/resources"))
    from(project(":myCarsApp").layout.projectDirectory.dir("src/jsMain/resources"))
    val carDetailWebpackDir =
        if (needsSplitBundleDevWebpack) {
            project(":carDetailApp").layout.buildDirectory.dir("kotlin-webpack/js/developmentExecutable")
        } else {
            project(":carDetailApp").layout.buildDirectory.dir("kotlin-webpack/js/productionExecutable")
        }
    from(carDetailWebpackDir) {
        include("carDetail.js", "carDetail.js.map")
    }
    val myCarsWebpackDir =
        if (needsSplitBundleDevWebpack) {
            project(":myCarsApp").layout.buildDirectory.dir("kotlin-webpack/js/developmentExecutable")
        } else {
            project(":myCarsApp").layout.buildDirectory.dir("kotlin-webpack/js/productionExecutable")
        }
    from(myCarsWebpackDir) {
        include("myCars.js", "myCars.js.map")
    }
}

tasks.named("jsBrowserDevelopmentRun").configure {
    dependsOn(
        ":carDetailApp:jsBrowserDevelopmentWebpack",
        ":myCarsApp:jsBrowserDevelopmentWebpack",
    )
}
