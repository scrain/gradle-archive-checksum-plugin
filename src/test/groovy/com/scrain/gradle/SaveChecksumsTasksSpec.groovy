package com.scrain.gradle

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

/**
 * Created by scrain on 7/24/16.
 */
class SaveChecksumsTasksSpec extends Specification {
    @Rule
    TemporaryFolder tempDir = new TemporaryFolder()

    Project project

    SaveChecksumsTask task

    File checksumsFile


    def setup() {
        project = ProjectBuilder.builder().withProjectDir(tempDir.newFolder()).build()
        project.extensions.create(ArchiveChecksumPlugin.PLUGIN_EXTENSION_NAME, ArchiveChecksumPluginExtension)
        task = project.tasks.create(ArchiveChecksumPlugin.TASKNAME_SAVE_CHECKSUMS, SaveChecksumsTask)

        checksumsFile = tempDir.newFile()
        task.pluginExt.checksumsPropertyFile = project.relativePath checksumsFile
    }

    def 'When checksums file does not exist, the file is created and the new checksum is written'() {
        when:
        task.pluginExt.checksums.put('foo', 'bar')
        task.saveChecksums()

        then:
        checksumsFile.text.contains('foo=bar')
    }

    def 'When checksums file exists but does not contain a previous checksum, the new checksum is written'() {
        when:
        checksumsFile << 'bar=foo'
        task.pluginExt.checksums.put('foo', 'bar')
        task.saveChecksums()

        then:
        checksumsFile.text.contains('bar=foo')
        checksumsFile.text.contains('foo=bar')
    }

    def 'When checksums file exists and contain a previous checksum, the new checksum is written'() {
        when:
        checksumsFile << ' foo = oldbar'
        task.pluginExt.checksums.put('foo', 'bar')
        task.saveChecksums()

        then:
        checksumsFile.text.contains('foo = bar')
        !checksumsFile.text.contains('oldbar')
    }

    def 'When checksums file is a folder, an exception is thrown'() {
        when:
        checksumsFile = tempDir.newFolder()
        task.pluginExt.checksumsPropertyFile = project.relativePath checksumsFile
        task.pluginExt.checksums.put('foo', 'bar')
        task.saveChecksums()

        then:
        thrown(GradleException)
    }
}
