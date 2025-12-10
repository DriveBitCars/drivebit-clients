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

// Отключаем Android unit тесты для всех модулей с Android target, если SDK не установлен
// Это позволяет запускать check без Android SDK в KMP проектах
afterEvaluate {
    val hasAndroidSdk = 
        project.hasProperty("android.sdk.dir") || 
        System.getenv("ANDROID_HOME") != null ||
        (file("local.properties").exists() && 
         file("local.properties").readText().contains("sdk.dir=") &&
         !file("local.properties").readText().contains("# sdk.dir="))

    if (!hasAndroidSdk) {
        subprojects.forEach { subproject ->
            try {
                // Отключаем Android unit тесты
                subproject.tasks.matching { 
                    it.name.contains("test", ignoreCase = true) && 
                    (it.name.contains("Android", ignoreCase = true) || 
                     it.name.contains("UnitTest", ignoreCase = true) ||
                     it.name.contains("DebugUnitTest", ignoreCase = true))
                }.configureEach {
                    enabled = false
                }
            } catch (e: Exception) {
                // Игнорируем ошибки для модулей без Android плагина
            }
        }
    }
}

ktlint {
    ktLintConfig()
}

// ktlint tasks are configured in subprojects

// Автоматическое обновление yarn lock перед сохранением
// Это решает проблему "Lock file was changed" раз и навсегда
afterEvaluate {
    tasks.matching { it.name == "kotlinStoreYarnLock" }.configureEach {
        doFirst {
            // Автоматически обновляем yarn lock перед сохранением
            try {
                tasks.named("kotlinUpgradeYarnLock").get().actions.forEach { action ->
                    action.execute(tasks.named("kotlinUpgradeYarnLock").get())
                }
            } catch (e: Exception) {
                // Игнорируем ошибки при обновлении
            }
        }
    }
}
