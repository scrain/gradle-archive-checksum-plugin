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
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.logging.Logger

/**
 *  Main plugin implementation for the gradle checksum-plugin
 */
class ChecksumPlugin implements Plugin<Project> {
    protected final static String TASK_GROUP = 'Checksum Tasks'

    private ChecksumExtension checksumExt

    private Logger logger

    void apply(Project project) {
        logger = project.logger

        checksumExt = project.extensions.create(ChecksumExtension.NAME, ChecksumExtension, project)

        Task computeChecksums = project.tasks.create(ComputeChecksumsTask.NAME, ComputeChecksumsTask)

        project.tasks.withType(SourceChecksumTask).whenTaskAdded { checksumTask ->
            computeChecksums.dependsOn checksumTask
        }

        SaveChecksumsTask saveChecksums = project.tasks.create(SaveChecksumsTask.NAME, SaveChecksumsTask)
        saveChecksums.dependsOn computeChecksums

        project.afterEvaluate {
            createChecksumTasks(project)
        }

    }

    protected List<SourceChecksumTask> createChecksumTasks(Project project) {
        List<SourceChecksumTask> tasks = []

        project.tasks.all { task ->
            ChecksumItem item = checksumExt.tasks.findByName(task.name)
            if (item) {
                logger.lifecycle ":checksum-plugin item - ${item}"
                SourceChecksumTask checksumTask = createChecksumTask(project, item, task)
                tasks << checksumTask

                checksumTask.configureChecksumSource(task, item.source ?: checksumExt.defaultSource)
            }
        }
        tasks
    }

    protected SourceChecksumTask createChecksumTask(Project project, ChecksumItem item, Task task) {
        validateTaskForChecksum(task)

        String checksumTaskName = checksumExt.checksumTaskName(item)

        logger.lifecycle ":checksum-plugin configuring checksum task '${checksumTaskName}'"

        SourceChecksumTask checksumTask = project.tasks.create(checksumTaskName, SourceChecksumTask)

        checksumTask.description = "Generates checksum for task '${task.name}'"
        checksumTask.propertyName = checksumExt.checksumPropertyName(item)

        checksumTask
    }

    protected void validateTaskForChecksum(Task task) {
        if (task instanceof SourceChecksumTask ||
            task instanceof SaveChecksumsTask ||
            task instanceof ComputeChecksumsTask) {

            throw new GradleException("Task '${task.name}' is a checksum plugin task")
        }
    }


}
