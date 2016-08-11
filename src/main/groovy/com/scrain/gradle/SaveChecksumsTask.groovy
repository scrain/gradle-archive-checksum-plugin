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

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

class SaveChecksumsTask extends DefaultTask {
    protected static final String NAME = 'saveChecksums'

    String group = ChecksumPlugin.TASK_GROUP

    private ChecksumExtension checksumExt = project.extensions.findByName(ChecksumExtension.NAME)

    @TaskAction
    def save() {
        File checksumsFile = createChecksumsFile()

        Properties existingProperties = loadProperties(checksumsFile)

        project.tasks.withType(SourceChecksumTask) {
            writeChecksum(checksumsFile, it.propertyName, it.checksum, existingProperties.containsKey(it.propertyName))
        }
    }

    private File createChecksumsFile() {
        File checksumsFile = project.file checksumExt.propertyFile

        if (!checksumsFile.exists()) {
            logger.info ":${name} checksums file does not exist, creating"
            checksumsFile.parentFile.mkdirs()
            checksumsFile.createNewFile()
        } else if (!checksumsFile.isFile()) {
            throw new GradleException("checksumsFile ${checksumExt.propertyFile} is a directory!")
        }
        checksumsFile
    }

    protected void writeChecksum(File file, String key, String value, boolean keyAlreadyExists) {
        value = value ?: ''
        if (keyAlreadyExists) {
            logger.info( ":${name} updating: ${key}=${value}")
            project.ant.replaceregexp(file: file, byline: true) {
                regexp(pattern: "^(\\s*)$key((\\s*[=|:]\\s*)|(\\s+)).+\$")
                substitution(expression: "\\1$key\\2$value")
            }
        } else {
            logger.info( ":${name} adding:   ${key}=${value}")
            file << "\n${key}=${value}"
        }
    }

    private Properties loadProperties(File file) {
        Properties props = new Properties()
        props.load(file.newReader())
        props
    }
}
