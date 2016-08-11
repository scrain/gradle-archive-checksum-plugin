/*
 * Copyright [2016] Shawn Crain
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.scrain.gradle

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class SaveChecksumsTasksSpec extends Specification {
    @Rule
    TemporaryFolder tempDir = new TemporaryFolder()

    Project project

    SourceChecksumTask checksumTask

    SaveChecksumsTask saveChecksumsTask

    File checksumsFile

    def setup() {
        project = ProjectBuilder.builder().withProjectDir(tempDir.newFolder()).build()
        project.extensions.create(ChecksumExtension.NAME, ChecksumExtension)

        checksumTask = project.tasks.create('testChecksum', SourceChecksumTask)
        checksumTask.checksum = '123'
        checksumTask.propertyName = 'testChecksum'

        saveChecksumsTask = project.tasks.create(SaveChecksumsTask.NAME, SaveChecksumsTask)

        checksumsFile = project.file project.checksum.propertyFile
    }

    def 'When checksums file does not exist, it is created and the new checksum is written'() {
        when:
            assert checksumsFile.exists() == false
            saveChecksumsTask.save()

        then:
            checksumsFile.exists()
            checksumsFile.text.contains "testChecksum=123"
    }

    def 'When checksums file exists but does not contain a previous checksum, the new checksum is written'() {
        when:
            checksumsFile << PROPFILE_COMMENTS
            checksumsFile << 'bar=foo'
            checksumsFile << PROPFILE_OTHER_PROPERTIES
            saveChecksumsTask.save()

        then:
            checksumsFile.text.contains(PROPFILE_COMMENTS)
            checksumsFile.text.contains(PROPFILE_OTHER_PROPERTIES)
            checksumsFile.text.contains('bar=foo')
            checksumsFile.text.contains('testChecksum=123')
    }

    def 'When checksums file exists and contain a previous checksum, the new checksum is written'() {
        when:
            checksumsFile << PROPFILE_COMMENTS
            checksumsFile << ' testChecksum = oldbar' // make sure extra whitespace can
            checksumsFile << PROPFILE_OTHER_PROPERTIES
            saveChecksumsTask.save()

        then:
            checksumsFile.text.contains(PROPFILE_COMMENTS)
            checksumsFile.text.contains(PROPFILE_OTHER_PROPERTIES)
            checksumsFile.text.contains('testChecksum = 123')
            !checksumsFile.text.contains('oldbar')
    }

    def 'When checksums file exists and contain a previous checksum with empty value, the new checksum is written'() {
        when:
            checksumsFile << PROPFILE_COMMENTS
            checksumsFile << 'testChecksum='
            checksumsFile << PROPFILE_OTHER_PROPERTIES
            saveChecksumsTask.save()

        then:
            checksumsFile.text.contains(PROPFILE_COMMENTS)
            checksumsFile.text.contains(PROPFILE_OTHER_PROPERTIES)
            checksumsFile.text.contains('testChecksum=123')
    }

    def 'When checksums file is a folder, GradleException is thrown'() {
        when:
            checksumsFile = tempDir.newFolder()
            project.checksum.propertyFile = project.relativePath checksumsFile
            saveChecksumsTask.save()

        then:
            thrown(GradleException)
    }

    final static String PROPFILE_COMMENTS = """

# comments with testChecksum=XXX in them

# testChecksum=XXX at the start

# at the end testChecksum=XXX

# testChecksum=XXX

"""

    final static String PROPFILE_OTHER_PROPERTIES = """

# other properties
unrelated=1234
version=3456

# a couple that might trip us up
Not-testChecksum=XXX
testChecksum_not=XXX

"""

}
