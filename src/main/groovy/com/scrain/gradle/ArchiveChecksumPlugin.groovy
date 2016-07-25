package com.scrain.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.bundling.AbstractArchiveTask

class ArchiveChecksumPlugin implements Plugin<Project> {
    static final String TASKNAME_COMPUTE_CHECKSUMS = "computeArchiveChecksums"

    static final String TASKNAME_SAVE_CHECKSUMS = "saveArchiveChecksums"

    static final String PLUGIN_EXTENSION_NAME = "archiveChecksum"

    protected ArchiveChecksumPluginExtension pluginExt

    void apply(Project project) {
        pluginExt = project.extensions.create PLUGIN_EXTENSION_NAME, ArchiveChecksumPluginExtension

        Task computeChecksums = project.tasks.create TASKNAME_COMPUTE_CHECKSUMS, this.&computeChecksums

        Task saveChecksums = project.tasks.create TASKNAME_SAVE_CHECKSUMS, SaveChecksumsTask

        saveChecksums.dependsOn computeChecksums

        project.tasks.withType(AbstractArchiveTask) { archiveTask ->

            Task task = project.tasks.create("${archiveTask.name}Checksum", ArchiveChecksumTask) {
                it.archiveTask       = archiveTask
                it.checksumsDir      = project.file("${project.buildDir}/checksums/${archiveTask.name}")
                it.sourceFiles       = archiveTask.source
            }

            computeChecksums.dependsOn << task
        }
    }

    def computeChecksums() {

    }
}