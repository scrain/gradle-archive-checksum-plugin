package com.scrain.gradle

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

/**
 * Created by scrain on 7/24/16.
 */
class ArchiveChecksumPluginSpec extends Specification {

    def "When plugin is applied then the extension and default tasks are created"() {
        when:
        Project project = ProjectBuilder.builder().build()
        project.pluginManager.apply("com.scrain.archive-checksum-plugin")

        then:
        project.extensions.findByName(ArchiveChecksumPlugin.PLUGIN_EXTENSION_NAME)
        project.tasks.findByName(ArchiveChecksumPlugin.TASKNAME_COMPUTE_CHECKSUMS)
        project.tasks.findByName(ArchiveChecksumPlugin.TASKNAME_SAVE_CHECKSUMS)
    }

    def "When plugin is applied with java plugin then checksum task is created for the jar task"() {
        when:
        Project project = ProjectBuilder.builder().build()
        project.pluginManager.apply("com.scrain.archive-checksum-plugin")
        project.pluginManager.apply("java")

        then:
        ArchiveChecksumTask task = project.tasks.findByName("jarChecksum")
        task.archiveTask.name == "jar"
    }
}
