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
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification


class ChecksumPluginSpec extends Specification {

    def "When plugin is applied then the extension and default tasks are created"() {
        when:
        Project project = ProjectBuilder.builder().build()
        project.pluginManager.apply("com.scrain.checksum-plugin")

        then:
        project.extensions.findByName(ChecksumExtension.NAME)
        project.tasks.findByName(ComputeChecksumsTask.NAME)
        project.tasks.findByName(SaveChecksumsTask.NAME)
    }

    def "When plugin is applied with java plugin then checksum task is created for the jar task"() {
        when:
        Project project = ProjectBuilder.builder().build()
        project.pluginManager.apply("com.scrain.checksum-plugin")
        project.pluginManager.apply("java")

        then:
        Task task = project.tasks.findByName("jarChecksum")
        // task.archiveTask.name == "jar"
    }
}
