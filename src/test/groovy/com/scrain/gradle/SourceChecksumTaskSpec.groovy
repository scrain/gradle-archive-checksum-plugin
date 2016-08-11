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

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class SourceChecksumTaskSpec extends Specification {
    @Rule
    TemporaryFolder tempDir = new TemporaryFolder()

    Project project

    SourceChecksumTask task

    def setup() {
        File buildDir = tempDir.newFolder()

        project = ProjectBuilder.builder().withProjectDir(buildDir).build()

        project.extensions.create(ChecksumExtension.NAME, ChecksumExtension)
        task = project.tasks.create('sourceChecksum', SourceChecksumTask)
    }

    def "sourceFileList should return a sorted list of file paths relative to the project dir"() {
        when:
            task.source = project.files(createSourceFiles())
            List sourceFileList = task.sourceFileList
            File sourceFileListing = task.createSourceFileListing()

        then:
            sourceFileList == ['a', 'b', 'c']
            sourceFileListing.text == 'a\nb\nc'
    }

    def "checksums should be non-blank and consistent for the same source files"() {
        when: 'A checksum is produced'
            def sourceFiles = createSourceFiles()
            task.source = project.files(sourceFiles)
            task.compute()
            String firstChecksum = task.checksum

        and: 'Project is reset with the same source files and checksum is recomputed'
            assert project.buildDir.deleteDir()
            sourceFiles.each { assert it.delete() }
            createSourceFiles()
            task.compute()
            String secondChecksum = task.checksum

        then: 'checksums are consistent'
            firstChecksum == secondChecksum
    }

    def "A source file renaming should yield a different checksum"() {
        when: 'A checksum is produced'
            File sourceFile = createFile("${project.projectDir}/foo.txt")
            task.source = project.file(sourceFile)
            task.compute()
            String firstChecksum = task.checksum

        and: 'Source file is renamed and the checksum recomputed'
            assert project.buildDir.deleteDir(), 'could not delete project.buildDir'
            assert sourceFile.renameTo("${project.projectDir}/bar.txt"), 'could not rename test file'
            task.source = project.file("${project.projectDir}/bar.txt")
            task.compute()
            String secondChecksum = task.checksum

        then: 'The checksums should be different'
            firstChecksum != secondChecksum
    }

    private File[] createSourceFiles() {
        [createFile("${project.projectDir}/c"),
         createFile("${project.projectDir}/a"),
         createFile("${project.projectDir}/b")]
    }

    private File createFile(String name) {
        File file = new File(name)
        file.createNewFile()
        file << name
        file
    }
}
