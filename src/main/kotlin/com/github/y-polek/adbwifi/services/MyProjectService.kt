package com.github.y-polek.adbwifi.services

import com.intellij.openapi.project.Project
import com.github.y-polek.adbwifi.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
