import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType.AndroidStudio
import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType.IntellijIdea
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.intellij.platform.gradle.models.ProductRelease.Channel.*
import org.jetbrains.kotlin.gradle.internal.ensureParentDirsCreated
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import kotlin.time.Duration.Companion.seconds

plugins {
    id("java")
    alias(libs.plugins.kotlin)
    alias(libs.plugins.intelliJPlatform)
    alias(libs.plugins.changelog)
    alias(libs.plugins.kover)
    alias(libs.plugins.sentry)
    alias(libs.plugins.detekt)
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

kotlin {
    jvmToolchain(21)

    sourceSets {
        main {
            kotlin.srcDir("build/generated/src/")
        }
    }
}

repositories {
    mavenCentral()
    // IntelliJ Platform Gradle Plugin Repositories Extension - read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-repositories-extension.html
    intellijPlatform {
        defaultRepositories()
    }
}

// Dependencies are managed with Gradle version catalog - read more: https://docs.gradle.org/current/userguide/platforms.html#sub:version-catalog
dependencies {
    detektPlugins(libs.detekt)

    testImplementation(libs.junit)
    testImplementation(libs.opentest4j)
    testImplementation(libs.assertj)
    testImplementation(libs.truth)

    // Remote Robot UI testing
    testImplementation(libs.remote.robot)
    testImplementation(libs.remote.fixtures)

    intellijPlatform {
        androidStudio("2025.2.3.9")
        bundledPlugin("org.jetbrains.android")

        testFramework(TestFrameworkType.Platform)
    }
}

// Configure IntelliJ Platform Gradle Plugin - read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-extension.html
intellijPlatform {
    pluginConfiguration {
        name = providers.gradleProperty("pluginName")
        version = providers.gradleProperty("pluginVersion")

        // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
        description = providers.fileContents(layout.projectDirectory.file("README.md")).asText.map {
            val start = "<!-- Plugin description -->"
            val end = "<!-- Plugin description end -->"

            with(it.lines()) {
                if (!containsAll(listOf(start, end))) {
                    throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                }
                subList(indexOf(start) + 1, indexOf(end)).joinToString("\n").let(::markdownToHTML)
            }
        }

        val changelog = project.changelog // local variable for configuration cache compatibility
        // Get the latest available change notes from the changelog file
        changeNotes = providers.gradleProperty("pluginVersion").map { pluginVersion ->
            with(changelog) {
                renderItem(
                    (getOrNull(pluginVersion) ?: getUnreleased())
                        .withHeader(false)
                        .withEmptySections(false),
                    Changelog.OutputType.HTML,
                )
            }
        }

        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild")
            untilBuild = provider { null }
        }
    }

    signing {
        certificateChain = providers.environmentVariable("CERTIFICATE_CHAIN")
        privateKey = providers.environmentVariable("PRIVATE_KEY")
        password = providers.environmentVariable("PRIVATE_KEY_PASSWORD")
    }

    publishing {
        token = providers.environmentVariable("PUBLISH_TOKEN")
        // The pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
        // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
        // https://plugins.jetbrains.com/docs/intellij/deployment.html#specifying-a-release-channel
        channels = providers.gradleProperty("pluginVersion").map { listOf(it.substringAfter('-', "").substringBefore('.').ifEmpty { "default" }) }
    }

    pluginVerification {
        ignoredProblemsFile = file("verifier/ignored-problems.txt")

        // https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-extension.html#-cbbl0j_1232
        //recommended()
        ides {
            select {
                types = listOf(IntellijIdea)
                channels = listOf(RELEASE)
                sinceBuild = providers.gradleProperty("pluginSinceBuild")
            }
            select {
                types = listOf(IntellijIdea)
                channels = listOf(EAP)
                sinceBuild = "261"
            }
            select {
                types = listOf(AndroidStudio)
                channels = listOf(RELEASE)
                sinceBuild = providers.gradleProperty("pluginSinceBuild")
            }
            select {
                types = listOf(AndroidStudio)
                channels = listOf(BETA, CANARY)
                sinceBuild = "261"
            }
        }
    }
}

// Configure Gradle Changelog Plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    groups.empty()
    repositoryUrl = providers.gradleProperty("pluginRepositoryUrl")
}

// Configure Gradle Kover Plugin - read more: https://github.com/Kotlin/kotlinx-kover#configuration
kover {
    reports {
        total {
            xml {
                onCheck = true
            }
        }
    }
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom("$projectDir/detekt/config.yml")
    baseline = file("$projectDir/detekt/baseline.xml")
}

tasks {
    wrapper {
        gradleVersion = providers.gradleProperty("gradleVersion").get()
    }

    publishPlugin {
        dependsOn(patchChangelog)
    }

    register<Task>("loadSentryProperties") {
        val secretsFile = file("build/generated/src/SentryProperties.kt").apply {
            ensureParentDirsCreated()
            createNewFile()
        }
        secretsFile.printWriter().use { writer ->
            writer.println("package dev.polek.adbwifi")
            writer.println()

            val sentryDsn = System.getenv("SENTRY_DSN")
            val sentryDsnStr = if (sentryDsn != null) "\"$sentryDsn\"" else "null"
            writer.println("val SENTRY_DSN: String? = $sentryDsnStr")
        }
    }
    withType<KotlinCompile>().configureEach {
        dependsOn("loadSentryProperties")
    }

    withType<Detekt>().configureEach {
        reports {
            html.required.set(true)
            sarif.required.set(true) // standardized SARIF format (https://sarifweb.azurewebsites.net/) to support integrations with GitHub Code Scanning
            md.required.set(false)
            xml.required.set(false)
            txt.required.set(false)
        }
    }

    // AndroidStudio releases list: https://plugins.jetbrains.com/docs/intellij/android-studio-releases-list.html
    val runAndroidStudio by intellijPlatformTesting.runIde.registering {
        type = AndroidStudio
        version = "2025.2.2.7"
    }

    val runIntellijIdea by intellijPlatformTesting.runIde.registering {
        type = IntellijIdea
        version = "2025.3.2"
    }

    // Run IDE with Robot Server for UI testing
    val runIdeForUiTests by intellijPlatformTesting.runIde.registering {
        plugins {
            robotServerPlugin()
        }
        task {
            jvmArgumentProviders += CommandLineArgumentProvider {
                listOf(
                    "-Drobot-server.port=8082",
                    "-Djb.privacy.policy.text=<!--999.999-->",
                    "-Djb.consents.confirmation.enabled=false",
                    "-Dide.trust.all.projects=true",
                    "-Dide.show.tips.on.startup.default.value=false",
                    "-Didea.ProcessCanceledException=disabled"
                )
            }
            // Open test project automatically
            argumentProviders += CommandLineArgumentProvider {
                listOf(layout.projectDirectory.dir("testData/testProject").asFile.absolutePath)
            }
        }
    }

}

// Run UI tests with IDE (starts IDE, runs tests, stops IDE)
tasks.register("runUiTests") {
    group = "verification"
    description = "Runs UI tests with Robot Server (starts IDE, runs tests, stops IDE)"

    // Capture values for configuration cache compatibility
    val projectPath = layout.projectDirectory.asFile.absolutePath

    doLast {
        fun isRobotServerRunning(): Boolean {
            val process = ProcessBuilder("curl", "-s", "-o", "/dev/null", "-w", "%{http_code}", "http://127.0.0.1:8082")
                .redirectErrorStream(true)
                .start()
            val result = process.inputStream.bufferedReader().readText().trim()
            process.waitFor()
            return result == "200"
        }

        val workDir = File(projectPath)

        try {
            // Start IDE in background
            logger.lifecycle("Starting IDE with Robot Server...")
            ProcessBuilder("bash", "-c", "./gradlew runIdeForUiTests &")
                .directory(workDir)
                .redirectErrorStream(true)
                .start()

            // Wait for Robot Server to be available
            logger.lifecycle("Waiting for Robot Server to be available on port 8082...")
            val maxWaitTime = 60.seconds.inWholeMilliseconds
            val startTime = System.currentTimeMillis()
            var serverReady = false

            while (System.currentTimeMillis() - startTime < maxWaitTime) {
                if (isRobotServerRunning()) {
                    serverReady = true
                    break
                }
                Thread.sleep(5000)
            }

            if (!serverReady) {
                throw GradleException("Robot Server did not start within ${maxWaitTime / 1000} seconds")
            }

            logger.lifecycle("Robot Server is ready. Running UI tests...")

            // Run UI tests with explicit test class patterns
            val testProcess = ProcessBuilder(
                "./gradlew", "test",
                "--tests", "dev.polek.adbwifi.uitest.*",
                "--no-configuration-cache",
                "--rerun",
                "--console=plain"
            )
                .directory(workDir)
                .inheritIO()
                .start()
            val exitCode = testProcess.waitFor()

            val reportFile = File(workDir, "build/reports/tests/test/index.html")
            if (exitCode != 0) {
                logger.error("UI tests failed. See report at: file://${reportFile.absolutePath}")
                throw GradleException("UI tests failed with exit code $exitCode")
            }

            logger.lifecycle("UI tests completed successfully")
            logger.lifecycle("Test report: file://${reportFile.absolutePath}")
        } finally {
            // Stop the IDE
            logger.lifecycle("Stopping IDE...")
            ProcessBuilder("pkill", "-f", "robot-server.port=8082")
                .directory(workDir)
                .start()
                .waitFor()
        }
    }
}
