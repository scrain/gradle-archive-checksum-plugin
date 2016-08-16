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

import static SourceConfig.*
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class ChecksumExtensionSpec extends Specification {
    @Shared
    Project project

    @Shared
    ChecksumExtension checksumExt

    def setup() {
        project = ProjectBuilder.builder().build()
        checksumExt = project.extensions.create(ChecksumExtension.NAME, ChecksumExtension, project)
    }

    @Unroll
    @SuppressWarnings('GStringExpressionWithinString')
    def "Checksum task template can be overridden"(ChecksumExtension ext, ChecksumItem item, String expectedName) {
        expect:
            ext.checksumTaskName(item) == expectedName

        where:
            ext                                                 | item                              | expectedName
            new ChecksumExtension()                             | new ChecksumItem('foo')           | 'fooChecksum'
            new ChecksumExtension(taskNameTemplate: 'x${task}') | new ChecksumItem('foo')           | 'xfoo'
            new ChecksumExtension()                             | new ChecksumItem(taskName: 'bar') | 'bar'
            new ChecksumExtension(taskNameTemplate: 'x${task}') | new ChecksumItem(taskName: 'bar') | 'bar'
    }

    @Unroll
    @SuppressWarnings('GStringExpressionWithinString')
    def "Checksum property template can be overridden"(ChecksumExtension ext, ChecksumItem item, String expectedName) {
        expect:
            ext.checksumPropertyName(item) == expectedName

        where:
            ext                                                     | item                                  | expectedName
            new ChecksumExtension()                                 | new ChecksumItem('foo')               | 'checksum.foo'
            new ChecksumExtension(propertyNameTemplate: 'x${task}') | new ChecksumItem('foo')               | 'xfoo'
            new ChecksumExtension()                                 | new ChecksumItem(propertyName: 'bar') | 'bar'
            new ChecksumExtension(propertyNameTemplate: 'x${task}') | new ChecksumItem(propertyName: 'bar') | 'bar'
    }

    @Unroll
    def "Source enum can be set using mixed case strings"() {
        expect:
            checksumExt.tasks( tasksClosure )
            checksumExt.tasks.getByName('task').source == expectedSource

        where:
            tasksClosure                           | expectedSource
                { -> task { } }                    | null
                { -> task { source 'auto' } }      | AUTO
                { -> task { source 'Auto' } }      | AUTO
                { -> task { source 'inputs' } }    | INPUTS
                { -> task { source 'Inputs' } }    | INPUTS
                { -> task { source 'outputs' } }   | OUTPUTS
                { -> task { source 'OUTPUTS' } }   | OUTPUTS
                { -> task { source 'both' } }      | BOTH
                { -> task { source 'Both' } }      | BOTH
    }

    def "ChecksumExtension can be configured DSL-style"() {

        when: 'when values are set without using equals'
            project.checksum {
                propertyFile         'a'
                taskNameTemplate     'b'
                propertyNameTemplate 'c'
                algorithm            'd'
                defaultSource        'Both'
                tasks {
                    taskOne {
                        source       'inputs'
                        taskName     'e'
                        propertyName 'f'
                    }
                }
            }

        then: 'closure populates extension values as expected'
            checksumExt.propertyFile         == 'a'
            checksumExt.taskNameTemplate     == 'b'
            checksumExt.propertyNameTemplate == 'c'
            checksumExt.algorithm            == 'd'
            checksumExt.defaultSource        == BOTH
            checksumExt.tasks.taskOne.source       == INPUTS
            checksumExt.tasks.taskOne.taskName     == 'e'
            checksumExt.tasks.taskOne.propertyName == 'f'


        when: 'when values are set using equals'
            project.checksum {
                propertyFile         = '1'
                taskNameTemplate     = '2'
                propertyNameTemplate = '3'
                algorithm            = '4'
                defaultSource        = 'Inputs'
                tasks {
                    taskOne {
                        source       = 'Outputs'
                        taskName     = '5'
                        propertyName = '6'
                    }
                }
            }

        then: 'closure populates extension values as expected'
            checksumExt.propertyFile         == '1'
            checksumExt.taskNameTemplate     == '2'
            checksumExt.propertyNameTemplate == '3'
            checksumExt.algorithm            == '4'
            checksumExt.defaultSource        == INPUTS
            checksumExt.tasks.taskOne.source       == OUTPUTS
            checksumExt.tasks.taskOne.taskName     == '5'
            checksumExt.tasks.taskOne.propertyName == '6'

    }

}
