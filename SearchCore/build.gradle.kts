plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.ktlint)
}

kotlin {
    androidTarget {
        publishLibraryVariants("release", "debug")
    }

    js(IR) {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":Utils"))
            implementation(project(":Network"))
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.core)
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.android)
        }
        androidUnitTest.dependencies {
            implementation(libs.kotlin.testJunit)
            implementation(libs.junit)
        }
        jsMain.dependencies {
            implementation(libs.ktor.client.js)
        }
    }
}

android {
    namespace = "my.drivebit.search.core"
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

ktlint {
    android.set(false)
    ignoreFailures.set(true)
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
    }
    filter {
        exclude("**/generated/**")
        exclude("**/build/**")
    }
}
