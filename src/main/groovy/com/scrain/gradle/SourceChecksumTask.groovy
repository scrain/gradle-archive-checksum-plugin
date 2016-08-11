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


/**
 * Class used for implementing individual checksum computation tasks.  Checksum computation is done against
 * either a task's source file collection or output depending on how configured by the user.
 */
class SourceChecksumTask extends SourceTask {
    private final ChecksumExtension checksumExt = project.extensions.findByName(ChecksumExtension.NAME)

    String group = ChecksumPlugin.TASK_GROUP

    @OutputDirectory
    File checksumsDir = project.file "${project.buildDir}/checksums/${name}"

    String checksum

    /**
     * Name of property for which the checksum value should be saved under within the checksum propertyfile
     */
    private String propertyName

    String getPropertyName() {
        propertyName
    }

    void setPropertyName(String propertyName) {
        if (!propertyName) {
            throw new IllegalArgumentException('propertyName is required')
        }
        this.propertyName = propertyName
    }

    @TaskAction
    def compute() {
        logger.lifecycle ":${name} calculating checksum"

        checksumsDir.mkdirs()

        File sourceFileListing = createSourceFileListing()

        logger.lifecycle(":${name} files included:")

        String totalProp = "${name}.total.checksum.${System.nanoTime()}"

        ant.checksum(totalproperty: totalProp, algorithm: checksumExt.algorithm,  todir: checksumsDir) {
            fileset(dir: project.projectDir) {
                logger.lifecycle(":${name}   ${project.relativePath(sourceFileListing)}")
                include name: project.relativePath(sourceFileListing)
                source.each {
                    logger.lifecycle ":${name}   ${project.relativePath(it)}"
                    include name: project.relativePath(it)
                }
            }
        }
        checksum = ant.properties[totalProp]

        logger.lifecycle ":${name} result: ${checksum}"
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
    protected List<String> getSourceFileList() {
        source.collect{ project.relativePath(it) }.sort()
    }

    /**
     * Creates a file in the configured checksums location containing a sorted list of source files.
     *
     * @return File instance of the newly created source file listing
     */
    protected File createSourceFileListing() {
        File fileListing = project.file "${checksumsDir}/source-files.txt"

        if (fileListing.exists()) {
            fileListing.delete()
        }

        fileListing.parentFile.mkdirs()

        fileListing.createNewFile()

        fileListing << sourceFileList.join('\n')

        fileListing
    }
}
