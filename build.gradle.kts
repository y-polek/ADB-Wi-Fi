import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.kotlin.gradle.internal.ensureParentDirsCreated
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun properties(key: String) = project.findProperty(key).toString()

plugins {
    // Java support
    id("java")
    // Kotlin support
    id("org.jetbrains.kotlin.jvm") version "1.7.0"
    // gradle-intellij-plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
    id("org.jetbrains.intellij") version "1.13.3"
    // gradle-changelog-plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
    id("org.jetbrains.changelog") version "1.3.1"
    // detekt linter - read more: https://detekt.github.io/detekt/kotlindsl.html
    id("io.gitlab.arturbosch.detekt") version "1.22.0"
    // ktlint linter - read more: https://github.com/JLLeitschuh/ktlint-gradle
    id("org.jlleitschuh.gradle.ktlint") version "10.2.1"
    // Sentry - read more: https://docs.sentry.io/platforms/java/
    id("io.sentry.jvm.gradle") version "4.6.0"
}

group = properties("pluginGroup")
version = properties("pluginVersion")

// Configure project's dependencies
repositories {
    mavenCentral()
}
dependencies {
    testImplementation("org.assertj:assertj-core:3.26.0")

    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.22.0")
}

// Configure gradle-intellij-plugin plugin.
// Read more: https://github.com/JetBrains/gradle-intellij-plugin
intellij {
    pluginName.set(properties("pluginName"))
    version.set(properties("platformVersion"))
    type.set(properties("platformType"))
    downloadSources.set(properties("platformDownloadSources").toBoolean())
    updateSinceUntilBuild.set(false)

    // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file.
    plugins.set(properties("platformPlugins").split(',').map(String::trim).filter(String::isNotEmpty))
}

kotlin {
    sourceSets {
        main {
            kotlin.srcDir("build/generated/src/")
        }
    }
}

// Configure gradle-changelog-plugin plugin.
// Read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    version.set(properties("pluginVersion"))
}

// Configure detekt plugin.
// Read more: https://detekt.github.io/detekt/kotlindsl.html
detekt {
    config = files("./detekt-config.yml")
    buildUponDefaultConfig = true
}

// Configure ktlint-gradle plugin.
// Read more: https://github.com/JLLeitschuh/ktlint-gradle
ktlint {
    version.set("0.42.1")
    disabledRules.set(setOf("no-wildcard-imports", "import-ordering"))
}

tasks {
    // Set the JVM compatibility versions
    properties("javaVersion").let { javaVersion ->
        withType<JavaCompile> {
            sourceCompatibility = javaVersion
            targetCompatibility = javaVersion
        }
        withType<KotlinCompile> {
            kotlinOptions.jvmTarget = javaVersion
        }
        withType<Detekt> {
            jvmTarget = javaVersion
        }
    }

    register("loadSentryProperties") {
        val secretsFile = file("build/generated/src/SentryProperties.kt").apply {
            ensureParentDirsCreated()
            createNewFile()
        }
        secretsFile.printWriter().use { writer ->
            writer.println("package dev.polek.adbwifi")
            writer.println()

            val sentryDns = System.getenv("SENTRY_DNS")
            val sentryDnsStr = if (sentryDns != null) "\"$sentryDns\"" else "null"
            writer.println("val SENTRY_DNS: String? = $sentryDnsStr")
        }
    }

    patchPluginXml {
        version.set(properties("pluginVersion"))
        sinceBuild.set(properties("pluginSinceBuild"))

        // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
        pluginDescription.set(
            File(projectDir, "README.md").readText().lines().run {
                val start = "<!-- Plugin description -->"
                val end = "<!-- Plugin description end -->"

                if (!containsAll(listOf(start, end))) {
                    throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                }
                subList(indexOf(start) + 1, indexOf(end))
            }.joinToString("\n").run { markdownToHTML(this) }
        )

        // Get the latest available change notes from the changelog file
        changeNotes.set(provider { changelog.getLatest().toHTML() })
    }

    publishPlugin {
        dependsOn("patchChangelog")
        token.set(System.getenv("PUBLISH_TOKEN"))
        // pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
        // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
        // https://plugins.jetbrains.com/docs/intellij/deployment.html#specifying-a-release-channel
        channels.set(listOf(properties("pluginVersion").split('-').getOrElse(1) { "default" }.split('.').first()))
    }

    runIde {
        // ideDir.set(file("/Applications/Android Studio.app/Contents"))
    }
}
