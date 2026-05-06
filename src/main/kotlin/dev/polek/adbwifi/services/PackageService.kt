package dev.polek.adbwifi.services

import com.android.tools.idea.IdeInfo
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiManager
import com.intellij.psi.xml.XmlFile

@Service(Service.Level.PROJECT)
class PackageService(private val project: Project) {

    /**
     * Returns all application IDs found in the project.
     * Tries Android Studio's AndroidModel API first, then falls back to file parsing.
     */
    fun getProjectPackageNames(): List<String> {
        val packages = mutableListOf<String>()

        // Try Android Model API first (most accurate, handles build variants)
        packages.addAll(getApplicationIdsFromAndroidModel())

        // Fallback to file parsing if Android API returned nothing
        if (packages.isEmpty()) {
            parseApplicationIdFromGradle()?.let { packages.add(it) }
        }
        if (packages.isEmpty()) {
            parseNamespaceFromGradle()?.let { packages.add(it) }
        }
        if (packages.isEmpty()) {
            parsePackageFromManifest()?.let { packages.add(it) }
        }

        return packages.distinct()
    }

    /**
     * Attempts to get application IDs using Android Studio's AndroidModel API.
     * Returns empty list if the Android plugin is not available or no Android modules are found.
     */
    private fun getApplicationIdsFromAndroidModel(): List<String> {
        return if (IdeInfo.getInstance().isAndroidStudio) {
            try {
                AndroidModelPackageProvider.getApplicationIds(project)
            } catch (_: Throwable) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    private fun parseApplicationIdFromGradle(): String? {
        val basePath = project.basePath ?: return null
        val paths = listOf(
            "$basePath/app/build.gradle.kts",
            "$basePath/app/build.gradle"
        )

        for (path in paths) {
            val file = LocalFileSystem.getInstance().findFileByPath(path) ?: continue
            try {
                val content = String(file.contentsToByteArray())
                val match = APPLICATION_ID_REGEX.find(content)
                if (match != null) {
                    return match.groupValues[1]
                }
            } catch (_: Exception) {
                // Continue to next file
            }
        }

        return null
    }

    private fun parseNamespaceFromGradle(): String? {
        val basePath = project.basePath ?: return null
        val paths = listOf(
            "$basePath/app/build.gradle.kts",
            "$basePath/app/build.gradle"
        )

        for (path in paths) {
            val file = LocalFileSystem.getInstance().findFileByPath(path) ?: continue
            try {
                val content = String(file.contentsToByteArray())
                val match = NAMESPACE_REGEX.find(content)
                if (match != null) {
                    return match.groupValues[1]
                }
            } catch (_: Exception) {
                // Continue to next file
            }
        }

        return null
    }

    private fun parsePackageFromManifest(): String? {
        val basePath = project.basePath ?: return null
        val manifestPaths = listOf(
            "$basePath/app/src/main/AndroidManifest.xml",
            "$basePath/src/main/AndroidManifest.xml"
        )

        for (path in manifestPaths) {
            val virtualFile = LocalFileSystem.getInstance().findFileByPath(path) ?: continue
            try {
                val psiFile = PsiManager.getInstance(project).findFile(virtualFile) as? XmlFile ?: continue
                val packageName = psiFile.rootTag?.getAttributeValue("package")
                if (packageName != null) {
                    return packageName
                }
            } catch (_: Exception) {
                // Continue to next file
            }
        }

        return null
    }

    private companion object {
        private val APPLICATION_ID_REGEX = """applicationId\s*[=]?\s*["']([^"']+)["']""".toRegex()
        private val NAMESPACE_REGEX = """namespace\s*[=]?\s*["']([^"']+)["']""".toRegex()
    }
}
