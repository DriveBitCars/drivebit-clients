import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.ktlint)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
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

        iosMain.dependencies {
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
            implementation(project(":Storage"))
            implementation(project(":CommonViewModels"))
            implementation(project(":UI-Components"))
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            // ViewModel поддержка для веб-таргета
            implementation("io.insert-koin:koin-compose-viewmodel-js:4.1.1")
        }

        commonMain.dependencies {
            implementation(project(":Storage"))
            implementation(project(":CommonViewModels"))
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
    }
}

android {
    namespace = "my.drivebit.mobile"
    compileSdk =
        libs.versions.android.compileSdk
            .get()
            .toInt()

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

// Копирование статических ресурсов (изображений)
tasks.register<Copy>("copyStaticResources") {
    from("${rootProject.projectDir}/images")
    into("$buildDir/dist/js/productionExecutable/images")
}

tasks.named("jsBrowserDistribution") {
    dependsOn("copyStaticResources")
}
