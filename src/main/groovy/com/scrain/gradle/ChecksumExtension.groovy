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

import groovy.text.SimpleTemplateEngine
import groovy.text.TemplateEngine
import org.gradle.api.NamedDomainObjectCollection
import org.gradle.api.Project

/**
 * Plugin extension for checksum plugin.
 */
class ChecksumExtension {
    protected static final String ALG_MD5 = 'md5'

    protected static final String ALG_SHA1 = 'sha1'

    protected static final String[] ALGORITHMS = [ALG_MD5, ALG_SHA1]

    protected static final String NAME = 'checksum'

    private final TemplateEngine engine = new SimpleTemplateEngine()

    ChecksumExtension() { }

    ChecksumExtension(Project project) {
        tasks = project.container(ChecksumItem)
    }

    String propertyFile = 'checksums.properties'

    @SuppressWarnings('GStringExpressionWithinString')
    String propertyNameTemplate = 'checksum.${name}'

    @SuppressWarnings('GStringExpressionWithinString')
    String taskNameTemplate = '${name}Checksum'

    private String algorithm = ALG_SHA1

    String getAlgorithm() {
        algorithm
    }

    void setAlgorithm(String algorithm) {
        if ( ALGORITHMS.contains(algorithm) ) {
            this.algorithm = algorithm
        } else {
            throw new IllegalArgumentException("algorithm '${algorithm}' is invalid.  Posible values: ${ALGORITHMS} ")
        }
    }

    NamedDomainObjectCollection<ChecksumItem> tasks

    @SuppressWarnings('ConfusingMethodName')
    void tasks(Closure closure) {
        tasks.configure(closure)
    }

    protected String checksumTaskName(ChecksumItem item) {
        if (item.taskName) {
            item.taskName
        } else {
            engine.createTemplate(taskNameTemplate).make( ['name': item.name] ).toString()
        }
    }

    protected String checksumPropertyName(ChecksumItem item) {
        if (item.propertyName) {
            item.propertyName
        } else {
            engine.createTemplate(propertyNameTemplate).make( ['name': item.name] ).toString()
        }
    }
}
