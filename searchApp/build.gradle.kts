plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.ktlint)
}

kotlin {
    js(IR) {
        browser {
            commonWebpackConfig {
                outputFileName = "appCompose.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        jsMain.dependencies {
            implementation(project(":SearchCore"))
            implementation(project(":Utils"))
            implementation(project(":DrivebitWeb"))
            implementation(project(":WebShell"))
            implementation(project(":AppHeader"))
            implementation(project(":Storage"))
            implementation(project(":Repositories"))
            implementation(project(":CommonViewModels"))
            implementation(project(":Network"))
            implementation(compose.html.core)
            implementation(compose.runtime)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koinComposeViewmodelJs)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.js)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
        }
        jsTest.dependencies {
            implementation(kotlin("test"))
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

tasks.withType<org.gradle.api.tasks.Copy>().configureEach {
    duplicatesStrategy = org.gradle.api.file.DuplicatesStrategy.INCLUDE
}

tasks.named<org.gradle.api.tasks.Copy>("jsProcessResources").configure {
    from(layout.projectDirectory.file("src/jsMain/resources/search-app-shell/index.html")) {
        into("search-shell")
    }
    from(rootProject.layout.projectDirectory.dir("vendor")) {
        into("vendor")
    }
}
