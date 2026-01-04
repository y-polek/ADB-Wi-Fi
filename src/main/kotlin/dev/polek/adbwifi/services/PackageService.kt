package dev.polek.adbwifi.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiManager
import com.intellij.psi.xml.XmlFile

@Service(Service.Level.PROJECT)
class PackageService(private val project: Project) {

    fun getPackageName(): String? {
        return parseApplicationIdFromGradle() ?: parsePackageFromManifest()
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
    }
}
