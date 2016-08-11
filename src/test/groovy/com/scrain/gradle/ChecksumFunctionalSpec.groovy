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

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class ChecksumFunctionalSpec extends Specification {
    @Rule
    TemporaryFolder tempDir = new TemporaryFolder()

    File projectDir

    File buildFile

    File gradlePropertiesFile

    def setup() {
        projectDir = tempDir.root
        gradlePropertiesFile = tempDir.newFile('gradle.properties')
        gradlePropertiesFile << '\nversion = 1.0'
        gradlePropertiesFile << '\nchecksum.jarChecksum=nochecksum'
    }

    private GradleRunner createAndConfigureGradleRunner(String... arguments) {
        String[] args = arguments + '--stacktrace'
        GradleRunner.create().withProjectDir(projectDir).withArguments(args).withPluginClasspath()
    }

    private BuildResult build(String... arguments) {
        createAndConfigureGradleRunner(arguments).build()
    }

    def 'A build with multiple checksum task should contain a checksum task for each'() {
        when:
        createBuildFile( """
            checksum {
                checksums {
                    jar { }
                    sourcesJar { }
                }
            }
        """)
        BuildResult result = build 'tasks', '--all'

        then:
        result.output.contains(ComputeChecksumsTask.NAME)
        result.output.contains(SaveChecksumsTask.NAME)
        result.output.contains('jarChecksum')
        result.output.contains('sourcesJarChecksum')
    }

    def 'Saving checksums creates a checksum file with a value for each archive'() {
        when:
        createBuildFile( """
            checksum {
                checksums {
                    jar { }
                    sourcesJar { }
                }
            }
        """)
        BuildResult result = build SaveChecksumsTask.NAME
        File checksumsFile = new File("${projectDir}/checksums.properties")

        then:
        checksumsFile.exists()
        checksumsFile.text.contains 'checksum.jar='
        checksumsFile.text.contains 'checksum.sourcesJar='
    }

    def 'Checksums can be saved to a file containing other unrelated values'() {
        when:
        createBuildFile( """
            checksum {
                propertyFile='gradle.properties'
                checksums {
                    jar { }
                }
            }
        """)
        BuildResult result = build SaveChecksumsTask.NAME
        File checksumsFile = new File("${projectDir}/gradle.properties")

        then:
        checksumsFile.text.contains 'version = 1.0'              // existing value left alone
        checksumsFile.text.contains 'checksum.jar='              // checksum property exists
        !checksumsFile.text.contains('checksum.jar=nochecksum')  // previous value overwritten
    }

    def 'Pluging extension allows overriding of defaults'() {

        when:
        createBuildFile( """
            checksum {
                propertyFile='foo/bar.properties'
                checksums {
                    jar { }
                }
            }
        """)
        BuildResult result = build SaveChecksumsTask.NAME
        File checksumsFile = new File("${projectDir}/foo/bar.properties")

        then:
        checksumsFile.exists()
        checksumsFile.text.contains 'checksum.jar='
    }

    private createBuildFile( String pluginExt ) {
        buildFile = tempDir.newFile('build.gradle')

        buildFile << """
            plugins {
                id 'groovy'
                id 'com.scrain.checksum-plugin'
            }

            ${pluginExt}

            archivesBaseName='test'

            task sourcesJar(type: Jar, dependsOn: classes) {
                classifier = 'sources'
                from sourceSets.main.allSource
            }

            artifacts {
                archives sourcesJar
            }
        """

    }
}
