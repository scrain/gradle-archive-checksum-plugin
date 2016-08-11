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

import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction


class SourceChecksumTask extends SourceTask {
    private ChecksumExtension checksumExt = project.extensions.findByName(ChecksumExtension.NAME)

    String group = ChecksumPlugin.TASK_GROUP

    @OutputDirectory
    File checksumsDir = project.file "${project.buildDir}/checksums/${name}"

    String checksum

    private String propertyName

    String getPropertyName() {
        return propertyName
    }

    void setPropertyName(String propertyName) {
        if (!propertyName) {
            throw new IllegalArgumentException('propertyName is required')
        }
        this.propertyName = propertyName
    }

    @TaskAction
    def compute() {
        println ":${name}: calculating checksum"

        checksumsDir.mkdirs()

        File sourceFileListing = createSourceFileListing()

        println(":${name} files included:")

        String totalProp = "total${name}"

        AntBuilder antBuilder = new AntBuilder()

        antBuilder.checksum(totalproperty: totalProp, algorithm: checksumExt.algorithm,  todir: checksumsDir) {
            fileset(dir: project.projectDir) {
                println(":${name}   ${project.relativePath(sourceFileListing)}")
                include name: project.relativePath(sourceFileListing)
                source.each {
                    println ":${name}   ${project.relativePath(it)}"
                    include name: project.relativePath(it)
                }
            }
        }
        checksum = antBuilder.properties[totalProp]

        println ":${name} result: ${checksum}"
    }

    /**
     * Creates a sorted list of file name/path strings for all source files.
     * File paths returned are relative to the projectDir.
     *
     * This list is included as part of the total checksum as a strategy for detecting
     * when files are moved or renamed.
     *
     * @return Sorted list of file path strings of all files
     */
    List<String> getSourceFileList() {
        source.collect{ project.relativePath(it) }.sort()
    }

    /**
     * Creates a file in the configured checksums location containing a sorted list of source files.
     *
     * @return
     */
    File createSourceFileListing() {
        File fileListing = project.file "${checksumsDir}/source-files.txt"

        fileListing.createNewFile()

        fileListing << sourceFileList.join('\n')

        fileListing
    }
}
