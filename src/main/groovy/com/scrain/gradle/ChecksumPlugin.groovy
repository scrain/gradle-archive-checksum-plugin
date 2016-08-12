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
import org.gradle.api.tasks.AbstractCopyTask

/**
 *  Main plugin implementation for the gradle checksum-plugin
 */
class ChecksumPlugin implements Plugin<Project> {
    protected final static String TASK_GROUP = 'Checksum Tasks'

    protected ChecksumExtension checksumExt

    void apply(Project project) {
        checksumExt = project.extensions.create(ChecksumExtension.NAME, ChecksumExtension, project)

        Task computeChecksums = project.tasks.create(ComputeChecksumsTask.NAME, ComputeChecksumsTask)

        Task saveChecksums = project.tasks.create(SaveChecksumsTask.NAME, SaveChecksumsTask)

        saveChecksums.dependsOn computeChecksums

        project.afterEvaluate {
            createChecksumTasks(project)
        }

        project.tasks.withType(SourceChecksumTask) {
            computeChecksums.dependsOn it
        }
    }

    protected List<SourceChecksumTask> createChecksumTasks(Project project) {
        List<SourceChecksumTask> tasks = []

        checksumExt.tasks.each { ChecksumItem item ->
            project.logger.lifecycle ":checksum-plugin item - ${item}"
            tasks << createChecksumTask(project, item)
        }

        tasks
    }

    protected SourceChecksumTask createChecksumTask( Project project, ChecksumItem item ) {
        Task task = findTaskForChecksumCalculation(project, item)

        String checksumTaskName = checksumExt.checksumTaskName(item)

        project.logger.lifecycle ":checksum-plugin configuring checksum task ${checksumTaskName}"

        SourceChecksumTask checksumTask = project.tasks.create(checksumTaskName, SourceChecksumTask)

        checksumTask.description  = "Generates checksum for ${item.useSource?'sources':'output'} of task '${task.name}'"
        checksumTask.propertyName = checksumExt.checksumPropertyName(item)

        checksumTask.source       = checksumSource(item, task)

        checksumTask
    }


    protected Object checksumSource(ChecksumItem item, Task task) {
        String useTaskSource = item.useSource ?: checksumExt.useTaskSource  // item overrides checksumExt

        if (ChecksumExtension.USE_SOURCE_AUTO.equalsIgnoreCase(useTaskSource)) {
            return taskHasSource(task) ? task.source : task
        }

        if (useTaskSource.toBoolean() && ! taskHasSource(task) ) {
            throw new GradleException(
                "Invalid checksum configuration: Task '${task.name}' has no source for computing a checksum."
            )
        }

        useTaskSource.toBoolean() ? task.source : task
    }

    private boolean taskHasSource( Task task ) {
        task instanceof AbstractCopyTask // TODO: other classes might support source?
    }

    protected Task findTaskForChecksumCalculation(Project project, ChecksumItem item) {
        Task task = project.tasks.findByName(item.name)

        if (!task) {
            throw new GradleException("Task '${item.name}' not found")
        }

        if (task instanceof SourceChecksumTask ||
                task instanceof SaveChecksumsTask ||
                task instanceof ComputeChecksumsTask) {

            throw new GradleException("Task '${item.name}' is a checksum plugin task")
        }

        task
    }


}
