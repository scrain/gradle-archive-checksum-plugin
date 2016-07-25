package com.scrain.gradle

import org.apache.tools.ant.BuildException
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

/**
 * Created by scrain on 7/24/16.
 */
class SaveChecksumsTask extends BaseTask {

    @TaskAction
    def saveChecksums() {
        File checksumsFile = project.file pluginExt.checksumsPropertyFile

        if ( !checksumsFile.exists() ) {
            checksumsFile.parentFile.mkdirs()
            checksumsFile.createNewFile()
        } else if (!checksumsFile.isFile()) {
            throw new GradleException("checksumsFile ${pluginExt.checksumsPropertyFile} is a directory")
        }

        pluginExt.checksums.each { String key, String value ->
            writeChecksum(checksumsFile, key, value)
        }
    }

    def writeChecksum(File file, String key, value) {
        logger.info( ":${name} writing ${key}=${value} to checksums file ${file}")
        try {
            if (! containsProperty(file, key)) {
                logger.info( ":${name} '${key}' not found, appending")
                file << "${key}=${value}\n"
            } else {
                project.ant.replaceregexp(file: file, byline: true) {
                    regexp(pattern: "^(\\s*)$key((\\s*[=|:]\\s*)|(\\s+)).+\$")
                    substitution(expression: "\\1$key\\2$value")
                }
            }
        } catch (BuildException be) {
            throw new GradleException("Unabled to write property '${key}' to ${file}" , be)
        }
    }

    boolean containsProperty(File file, String key ) {
        Properties props = new Properties()
        props.load(file.newReader())
        props.containsKey(key)
    }

}
