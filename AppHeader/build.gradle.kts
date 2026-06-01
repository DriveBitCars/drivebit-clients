plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.ktlint)
}

kotlin {
    js(IR) {
        browser()
    }

    sourceSets {
        jsMain.dependencies {
            implementation(compose.html.core)
            implementation(compose.runtime)
            implementation(compose.ui)
            implementation(project(":WebShell"))
            implementation(project(":Storage"))
            implementation(project(":CommonViewModels"))
            implementation(project(":Repositories"))
            implementation(project(":Network"))
            implementation(project(":Maps"))
            implementation(project(":UI-Components"))
            implementation(project(":Utils"))
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
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
