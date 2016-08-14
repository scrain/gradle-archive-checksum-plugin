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

import static com.scrain.gradle.SourceConfig.*
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class SourceChecksumTaskSpec extends Specification {
    @Rule
    TemporaryFolder tempDir = new TemporaryFolder()

    @Shared
    Project project

    @Shared
    SourceChecksumTask checksumTask

    @Shared
    File inputFile = createTempFile('input')

    @Shared
    File outputFile = createTempFile('output')

    def setup() {
        File buildDir = tempDir.newFolder()

        project = ProjectBuilder.builder().withProjectDir(buildDir).build()

        project.extensions.create(ChecksumExtension.NAME, ChecksumExtension)
        checksumTask = project.tasks.create('sourceChecksum', SourceChecksumTask)
    }

    def "sourceFileList should return a sorted list of file paths relative to the project dir"() {
        when:
            checksumTask.source = project.files(createSourceFiles())
            List sourceFileList = checksumTask.sourceFileList
            File sourceFileListing = checksumTask.createSourceFileListing()

        then:
            sourceFileList == ['a', 'b', 'c']
            sourceFileListing.text == 'a\nb\nc'
    }

    def "checksums should be non-blank and consistent for the same source files"() {
        when: 'A checksum is produced'
            def sourceFiles = createSourceFiles()
            checksumTask.source = project.files(sourceFiles)
            checksumTask.compute()
            String firstChecksum = checksumTask.checksum

        and: 'Project is reset with the same source files and checksum is recomputed'
            assert project.buildDir.deleteDir()
            sourceFiles.each { assert it.delete() }
            createSourceFiles()
            checksumTask.compute()
            String secondChecksum = checksumTask.checksum

        then: 'checksums are consistent'
            firstChecksum == secondChecksum
    }

    def "A source file renaming should yield a different checksum"() {
        when: 'A checksum is produced'
            File sourceFile = createFile("${project.projectDir}/foo.txt")
            checksumTask.source = project.file(sourceFile)
            checksumTask.compute()
            String firstChecksum = checksumTask.checksum

        and: 'Source file is renamed and the checksum recomputed'
            assert project.buildDir.deleteDir(), 'could not delete project.buildDir'
            assert sourceFile.renameTo("${project.projectDir}/bar.txt"), 'could not rename test file'
            checksumTask.source = project.file("${project.projectDir}/bar.txt")
            checksumTask.compute()
            String secondChecksum = checksumTask.checksum

        then: 'The checksums should be different'
            firstChecksum != secondChecksum
    }

    @Unroll
    def "checksumTask's source is configuration driven"() {
        when:
            Task task = createTask(taskInputs, taskOutputs)
            checksumTask.configureChecksumSource(task, sourceConfig)

        then:
            checksumTask.source.asList() == expectedSourceFiles

        where:
            sourceConfig | taskInputs | taskOutputs | expectedSourceFiles
            AUTO         | inputFile  | outputFile  | [inputFile]
            AUTO         | inputFile  | null        | [inputFile]
            AUTO         | null       | outputFile  | [outputFile]
            AUTO         | null       | null        | []

            BOTH         | inputFile  | outputFile  | [inputFile, outputFile]
            BOTH         | inputFile  | null        | [inputFile]
            BOTH         | null       | outputFile  | [outputFile]
            BOTH         | null       | null        | []

            INPUTS       | inputFile  | outputFile  | [inputFile]
            INPUTS       | inputFile  | null        | [inputFile]
            INPUTS       | null       | outputFile  | []
            INPUTS       | null       | null        | []

            OUTPUTS      | inputFile  | outputFile  | [outputFile]
            OUTPUTS      | inputFile  | null        | []
            OUTPUTS      | null       | outputFile  | [outputFile]
            OUTPUTS      | null       | null        | []
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

    private Task createTask(File inputFile, File outputFile) {
        Task task = project.tasks.create('testTask') { }
        if (inputFile) {
            task.inputs.files inputFile
        }
        if (outputFile) {
            task.outputs.files outputFile
        }
        task
    }

    static File createTempFile(String name) {
        File file = File.createTempFile(name, null)
        file.deleteOnExit()
        file
    }
}
