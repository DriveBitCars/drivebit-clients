plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.ktlint)
}

kotlin {
    js(IR) {
        browser {
            commonWebpackConfig {
                outputFileName = "auth.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        jsMain.dependencies {
            implementation(project(":DrivebitWeb"))
            implementation(project(":WebShell"))
            implementation(project(":AppHeader"))
            implementation(project(":Storage"))
            implementation(project(":Repositories"))
            implementation(project(":CommonViewModels"))
            implementation(compose.html.core)
            implementation(compose.runtime)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koinComposeViewmodelJs)
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

val authShellRoutes =
    listOf(
        "verify-otp",
        "login-by-phone",
        "login-by-mail",
        "login-by-password",
    )

tasks.named<org.gradle.api.tasks.Copy>("jsProcessResources").configure {
    from(rootProject.layout.projectDirectory.dir("vendor")) {
        into("vendor")
    }
    val shellTemplate = layout.projectDirectory.file("src/jsMain/resources/auth-app-shell/index.html")
    authShellRoutes.forEach { route ->
        from(shellTemplate) {
            into(route)
        }
    }
}
