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

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Shared
import spock.lang.Specification

class ChecksumPluginSpec extends Specification {
    private static final String TASK_WITH_SOURCE = 'jar'
    private static final String TASK_WITHOUT_SOURCE = 'publish'

    @Shared
    Project project

    @Shared
    ChecksumExtension checksumExt

    @Shared
    ChecksumPlugin plugin

    def setup() {
        project = ProjectBuilder.builder().build()
        project.pluginManager.apply("java")
        project.pluginManager.apply("maven-publish")
        project.pluginManager.apply("com.scrain.checksum-plugin")
        checksumExt = project.extensions.findByName(ChecksumExtension.NAME)
        plugin = project.plugins.findPlugin(ChecksumPlugin)
    }

    def "When plugin is applied then the extension and default tasks are created"() {
        when:
            ComputeChecksumsTask computeChecksumsTask = project.tasks.findByName(ComputeChecksumsTask.NAME)
            SaveChecksumsTask saveChecksumsTask = project.tasks.findByName(SaveChecksumsTask.NAME)
            computeChecksumsTask.computeChecksums()
            saveChecksumsTask.save()

        then:
            checksumExt
            computeChecksumsTask
            saveChecksumsTask
    }

    def "checksum tasks should be created for each configured ChecksumItem"() {
        when:
            checksumExt.tasks << new ChecksumItem(TASK_WITH_SOURCE)
            checksumExt.tasks << new ChecksumItem(TASK_WITHOUT_SOURCE)
            List<SourceChecksumTask> tasks = plugin.createChecksumTasks(project)

        then:
            tasks.size() == 2

    }
}
