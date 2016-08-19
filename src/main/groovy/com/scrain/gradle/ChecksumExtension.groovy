/*
 * Copyright [2016] Shawn Crain
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.scrain.gradle

import static com.scrain.gradle.SourceConfig.AUTO
import groovy.text.SimpleTemplateEngine
import groovy.text.TemplateEngine
import groovy.transform.ToString
import org.gradle.api.NamedDomainObjectCollection
import org.gradle.api.Project

/**
 * Plugin extension for checksum plugin.
 */
@ToString(ignoreNulls = true, includeNames = true)
@SuppressWarnings('ConfusingMethodName')
class ChecksumExtension {
    protected static final String NAME = 'checksum'

    private final TemplateEngine engine = new SimpleTemplateEngine()

    private final Project project

    ChecksumExtension() { }

    ChecksumExtension(Project project) {
        this.project = project
        this.tasks = project.container(ChecksumItem)
    }

    String propertyFile = 'checksums.properties'

    void propertyFile(propertyFile) {
        this.propertyFile = propertyFile
    }

    private SourceConfig defaultSource = AUTO

    SourceConfig getDefaultSource() {
        defaultSource
    }

    void setDefaultSource(defaultSource) {
        this.defaultSource = defaultSource?.toString().toUpperCase()
    }

    void defaultSource(sourceConfig) {
        this.setDefaultSource(sourceConfig)
    }

    @SuppressWarnings('GStringExpressionWithinString')
    String propertyNameTemplate = 'checksum.${task}'

    void propertyNameTemplate(propertyNameTemplate) {
        this.propertyNameTemplate = propertyNameTemplate
    }

    @SuppressWarnings('GStringExpressionWithinString')
    String taskNameTemplate = '${task}Checksum'

    void taskNameTemplate(taskNameTemplate) {
        this.taskNameTemplate = taskNameTemplate
    }

    String algorithm = 'sha1'

    void algorithm(algorithm) {
        this.algorithm = algorithm
    }

    NamedDomainObjectCollection<ChecksumItem> tasks

    void tasks(Closure closure) {
        tasks.configure(closure)
    }

    protected String checksumTaskName(ChecksumItem item) {
        item.taskName ?: engine.createTemplate(taskNameTemplate).make(['task': item.name]).toString()
    }

    protected String checksumPropertyName(ChecksumItem item) {
        item.propertyName ?: engine.createTemplate(propertyNameTemplate).make(['task': item.name]).toString()
    }

    protected Properties getProperties() {
        assert project, 'Project not set!'

        File file = project.file(propertyFile)

        Properties props = new Properties()

        if ( file.exists() ) {
            props.load(file.newReader())
        }

        props
    }
}

