plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.ktlint) apply true
    alias(libs.plugins.kotzilla) apply true
    id("io.gitlab.arturbosch.detekt") version "1.23.8" apply true
}

private val ktLintConfig: org.jlleitschuh.gradle.ktlint.KtlintExtension.() -> Unit = {
    debug.set(false)
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

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "io.gitlab.arturbosch.detekt")

    tasks.matching { it.name == "test" || it.name == "build" }.configureEach {
        dependsOn("ktlintFormat")
    }

    ktlint {
        ktLintConfig()
    }

    detekt {
        buildUponDefaultConfig = true
        allRules = false
        config.setFrom("$rootDir/detekt.yml")
        baseline = file("$rootDir/detekt-baseline.xml")
    }
}

ktlint {
    ktLintConfig()
}

// ktlint tasks are configured in subprojects
