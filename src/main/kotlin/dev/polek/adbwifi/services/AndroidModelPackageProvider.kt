package dev.polek.adbwifi.services

import com.android.tools.idea.projectsystem.getModuleSystem
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.modules

object AndroidModelPackageProvider {

    fun getApplicationIds(project: Project): List<String> {
        return project.modules.asSequence()
            .map { it.getModuleSystem() }
            .filter { it.isValidForAndroidRunConfiguration() }
            .mapNotNull { it.androidModel?.applicationId }
            .filter { it.isNotBlank() }
            .distinct()
            .toList()
    }
}
