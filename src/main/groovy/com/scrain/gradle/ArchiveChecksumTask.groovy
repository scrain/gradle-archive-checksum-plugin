package com.scrain.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.bundling.AbstractArchiveTask

/**
 * Created by scrain on 7/22/16.
 */
class ArchiveChecksumTask extends DefaultTask {

    // AbstractArchiveTask

    @TaskAction
    def calculateChecksum() {

    }

}
