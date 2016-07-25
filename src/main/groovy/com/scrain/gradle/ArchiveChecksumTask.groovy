package com.scrain.gradle

import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.bundling.AbstractArchiveTask

/**
 * Created by scrain on 7/22/16.
 */
class ArchiveChecksumTask extends BaseTask {
    AbstractArchiveTask archiveTask

    @OutputDirectory
    File checksumsDir

    @InputFiles
    FileCollection sourceFiles

//    @InputFile
//    File sourceFileListing

    @TaskAction
    def calculateChecksum() {
        println ":${name} calculating checksum for ${archiveTask.archiveName}"

        String checksumProperty = checksumPropertyName(archiveTask)
        checksumsDir.mkdirs()

        File sourceFileListing = createSourceFileListing()

        logger.info(":${name} files included:")

        ant.checksum(totalproperty: checksumProperty, todir: checksumsDir) {
            fileset(dir: project.projectDir) {
                logger.info(":${name}   ${project.relativePath(sourceFileListing)}")
                include name: project.relativePath(sourceFileListing)
                archiveTask.source.each {
                    logger.info ":${name}   ${project.relativePath(it)}"
                    include name: project.relativePath(it)
                }
            }
        }
        pluginExt.checksums.put checksumProperty, ant.properties[checksumProperty]
        // ant.properties.remove(checksumProperty)

        println ":${name} result: ${pluginExt.checksums[checksumProperty]}"
    }

    /**
     * Creates a file containing the list of source files to be part of the checksum calculation.
     * This file will be used as part of the overall artifact checksum.  This is used to force
     * the overall archive checksum to change when source file names are changed.
     *
     * The file listing paths are relative to the project and sorted to ensure consistent results
     * between runs.
     *
     * @return
     */
    private File createSourceFileListing() {
        File sourceFileListing = project.file "${checksumsDir}/${archiveTask.archiveName}.filelist.txt"
        assert ! sourceFileListing.exists(), "already exists: ${sourceFileListing}"

        List<String> sourceFileNames = sourceFiles.collect { project.relativePath(it) }.sort()
        sourceFileListing << sourceFileNames.join("\n")

        sourceFileListing
    }

    private String checksumPropertyName( AbstractArchiveTask task ) {
        "${pluginExt.checksumPrefix}${getArchiveName(task)}"
    }

    private String getArchiveName(AbstractArchiveTask task) {
        def parts = []
        if (task.baseName) {
            parts << task.baseName
        }
        if (task.appendix) {
            parts << task.appendix
        }
        if ( task.classifier ) {
            parts << task.classifier
        }
        String name = parts.join('-')
        if ( task.extension ) {
            name += ".${task.extension}"
        }

        name
    }
}