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

import org.gradle.api.Task
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction

/**
 * Class used for implementing individual checksum computation tasks.
 */
class ChecksumTask extends SourceTask {
    private final ChecksumExtension checksumExt = project.extensions.findByName(ChecksumExtension.NAME)

    @OutputDirectory
    File checksumsDir = project.file "${project.buildDir}/checksums/${name}"

    @OutputFile
    File getChecksumFile() {
        project.file "${checksumsDir}/checksum.${checksumExt.algorithm}"
    }

    @OutputFile
    File sourceListFile = project.file "${checksumsDir}/source-files.txt"

    /**
     * Name of property for which the checksum value should be saved under within the checksum propertyfile
     */
    private String propertyName

    @Input
    String getPropertyName() {
        propertyName
    }

    void setPropertyName(String propertyName) {
        if (!propertyName) {
            throw new IllegalArgumentException('propertyName is required')
        }
        this.propertyName = propertyName
    }

    ChecksumTask() {
        group = ChecksumPlugin.TASK_GROUP
    }

    @TaskAction
    def compute() {
        assert checksumsDir.exists() ?: checksumsDir.mkdirs(), "Unable to create '${checksumsDir}'"

        File sourceFileListing = createSourceFileListing()

        logger.info(":${name} files included:")

        String totalProp = "${name}.total.checksum.${System.nanoTime()}"

        ant.checksum(totalproperty: totalProp, algorithm: checksumExt.algorithm, todir: checksumsDir) {
            fileset(dir: project.projectDir) {
                logger.info(":${name}   ${project.relativePath(sourceFileListing)}")
                include name: project.relativePath(sourceFileListing)
                source.each {
                    logger.info ":${name}   ${project.relativePath(it)}"
                    include name: project.relativePath(it)
                }
            }
        }
        String checksum = ant.properties[totalProp]

        checksumFile << checksum

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
    protected List<String> sortedSourceFileList() {
        source.collect { project.relativePath(it) }.sort()
    }

    /**
     * Creates a file in the configured checksums location containing a sorted list of source files.
     *
     * @return File instance of the newly created source file listing
     */
    protected File createSourceFileListing() {
        assert ! sourceListFile.exists() || sourceListFile.delete(), "Unable to remove source listing ${sourceListFile}"

        assert sourceListFile.parentFile.exists() ?: sourceListFile.parentFile.mkdirs(),
            "Unable to create source listing dir ${sourceListFile.parentFile}"

        assert sourceListFile.createNewFile(), "Unable to create source listing ${sourceListFile}"

        sourceListFile << sortedSourceFileList().join('\n')

        sourceListFile
    }

    /**
     * Configures the source files using inputs and/or outputs of the given task depending on the provided sourceConfig
     * directive.
     *
     * @param task - Task from which to pull inputs and/or outputs.
     * @param sourceConfig - SourceConfig directive which ultimately drives source
     */
    protected void configureChecksumSource(Task task, SourceConfig sourceConfig) {
        if (sourceConfig.includeInputs(task)) {
            includeTaskInputsInSource(task)
        }
        if (sourceConfig.includeOutputs(task)) {
            includeTaskOutputsInSource(task)
        }
    }

    protected void includeTaskInputsInSource(Task task) {
        if (!task.inputs.hasInputs) {
            logger.warn(":${name} WARNING configuration calls to include inputs, but '${task.name}' has none!")
        } else {
            source task.inputs.files
        }
    }

    protected void includeTaskOutputsInSource(Task task) {
        if (!task.outputs.hasOutput) {
            logger.warn(":${name} WARNING configuration calls to include outputs, but '${task.name}' has none!")
        } else {
            source task.outputs.files
        }
    }

    /**
     * Convenience method for checking if task's latest computed checksum value is equal to the
     * value stored in the property file.  Requires the latest computed value is available and will
     * throw assertion error if it is not.
     *
     * @return true if task's checksum value is the same as found in the properties file, otherwise false
     */
    boolean sameAsPropertyFile() {
        assert checksumFile?.text, "Computed checksum for '${name}' task not found.  Has it been run?"

        String propertyFileChecksum=checksumExt.properties[this.propertyName]

        logger.info ":${name}.sameAsPropertyFile() - ${checksumFile.text} (latest computed value)"
        logger.info ":${name}.sameAsPropertyFile() - ${propertyFileChecksum} (stored in file)"

        checksumFile.text == propertyFileChecksum
    }
}