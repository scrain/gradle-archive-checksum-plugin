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
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class ChecksumPluginSpec extends Specification {

    private static final String TASK_WITH_SOURCE    = 'jar'
    private static final String TASK_WITHOUT_SOURCE = 'publish'

    @Shared
    Project project = ProjectBuilder.builder().build()

    @Shared
    ChecksumExtension ext

    @Shared
    ChecksumPlugin plugin

    def setup() {
        project.pluginManager.apply("java")
        project.pluginManager.apply("maven-publish")
        project.pluginManager.apply("com.scrain.checksum-plugin")
        ext = project.extensions.findByName(ChecksumExtension.NAME)
        plugin = project.plugins.findPlugin(ChecksumPlugin)
    }

    def "When plugin is applied then the extension and default tasks are created"() {
        when:
            ComputeChecksumsTask computeChecksumsTask = project.tasks.findByName(ComputeChecksumsTask.NAME)
            SaveChecksumsTask saveChecksumsTask = project.tasks.findByName(SaveChecksumsTask.NAME)
            computeChecksumsTask.computeChecksums()
            saveChecksumsTask.save()

        then:
            ext
            computeChecksumsTask
            saveChecksumsTask
    }

    def "checksum tasks should be created for each configured ChecksumItem"() {
        when:
            ext.tasks << new ChecksumItem(TASK_WITH_SOURCE)
            ext.tasks << new ChecksumItem(TASK_WITHOUT_SOURCE)
            List<SourceChecksumTask> tasks = plugin.createChecksumTasks(project)

        then:
            tasks.size() == 2

    }

    @Unroll
    def 'Checksum source should be driven by checksum configuration extensions'() {
        when:
            ext.useTaskSource = checksumUseSource
            ChecksumItem item = new ChecksumItem(name:taskName,useSource: itemUseSource)
            Task task = project.tasks.findByName(taskName)

        then: 'item level configuration should override checksum level'
            plugin.checksumSource(item,task) == expectTaskSource ? task.source : task

        where:
            taskName            |    checksumUseSource   |  itemUseSource   | expectTaskSource
            TASK_WITH_SOURCE    |    'auto'              |  null            | true
            TASK_WITH_SOURCE    |    'auto'              |  'auto'          | true
            TASK_WITH_SOURCE    |    'auto'              |  true            | true
            TASK_WITH_SOURCE    |    'auto'              |  false           | false
            TASK_WITH_SOURCE    |    true                |  null            | true
            TASK_WITH_SOURCE    |    true                |  'auto'          | true
            TASK_WITH_SOURCE    |    true                |  true            | true
            TASK_WITH_SOURCE    |    true                |  false           | false
            TASK_WITH_SOURCE    |    false               |  null            | false
            TASK_WITH_SOURCE    |    false               |  'auto'          | true
            TASK_WITH_SOURCE    |    false               |  true            | true
            TASK_WITH_SOURCE    |    false               |  false           | false
            TASK_WITHOUT_SOURCE |    'auto'              |  null            | false
            TASK_WITHOUT_SOURCE |    'auto'              |  'auto'          | false
            TASK_WITHOUT_SOURCE |    'auto'              |  false           | false
            TASK_WITHOUT_SOURCE |    true                |  false           | false
            TASK_WITHOUT_SOURCE |    true                |  'auto'          | false
            TASK_WITHOUT_SOURCE |    false               |  null            | false
            TASK_WITHOUT_SOURCE |    false               |  'auto'          | false
            TASK_WITHOUT_SOURCE |    false               |  false           | false
    }

    @Unroll
    def "Invalid checksum extension configuration should throw GradleException when obtaining checksum source"() {
        when:
            ext.useTaskSource = checksumUseSource
            ChecksumItem item = new ChecksumItem(name:taskName,useSource: itemUseSource)
            Task task = project.tasks.findByName(taskName)
            plugin.checksumSource(item,task)

        then:
            thrown GradleException

        where:
            taskName            |    checksumUseSource   |  itemUseSource
            TASK_WITHOUT_SOURCE |    'auto'              |  true
            TASK_WITHOUT_SOURCE |    true                |  null
            TASK_WITHOUT_SOURCE |    true                |  true
            TASK_WITHOUT_SOURCE |    false               |  true
    }

}
