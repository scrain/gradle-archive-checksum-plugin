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

class ChecksumExtension {
    private TemplateEngine engine = new SimpleTemplateEngine()

    ChecksumExtension() { }

    ChecksumExtension(Project project) {
        checksums = project.container(ChecksumItem)
    }

    protected static final String NAME = 'checksum'

    String propertyFile = 'checksums.properties'

    String propertyNameTemplate = 'checksum.${name}'

    String taskNameTemplate = '${name}Checksum'

    private String algorithm = 'sha1'

    String getAlgorithm() {
        return algorithm
    }

    void setAlgorithm(String algorithm) {
        if ( ['md5', 'sha1'].contains(algorithm) ) {
            this.algorithm = algorithm
        } else {
            throw new IllegalArgumentException("algorithm '${algorithm}' is invalid.  Posible values: 'md5', 'sha1' ")
        }
    }

    NamedDomainObjectCollection<ChecksumItem> checksums

    void checksums(Closure closure) {
        checksums.configure(closure)
    }

    protected String checksumTaskName(ChecksumItem item) {
        if (item.taskName) {
            item.taskName
        } else {
            engine.createTemplate(taskNameTemplate).make( ['name': item.name] ).toString()
        }
    }

    protected String checksumPropertyName(ChecksumItem item) {
        if (item.taskName) {
            item.taskName
        } else {
            engine.createTemplate(propertyNameTemplate).make( ['name': item.name] ).toString()
        }
    }
}
