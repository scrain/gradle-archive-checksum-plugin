package com.scrain.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.AbstractArchiveTask

class ArchiveChecksumPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.extensions.create("archiveChecksum", ArchiveChecksumPluginExtension)

        project.tasks.withType(AbstractArchiveTask).each {

        }
    }
}