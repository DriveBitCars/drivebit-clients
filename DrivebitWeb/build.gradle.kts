plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.kotlinxSerialization)
}

kotlin {
    js(IR) {
        browser()
    }

    sourceSets {
        jsMain.dependencies {
            implementation(compose.html.core)
            implementation(compose.runtime)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)
            implementation(project(":WebShell"))
            implementation(project(":AppHeader"))
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
        jsTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(project(":Network"))
        }
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
