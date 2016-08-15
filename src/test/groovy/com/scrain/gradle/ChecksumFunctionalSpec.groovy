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

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import groovy.io.FileType
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Shared
import spock.lang.Specification

class ChecksumFunctionalSpec extends Specification {
    @Rule
    TemporaryFolder tempDir = new TemporaryFolder()

    @Shared
    File projectDir

    @Shared
    File buildFile

    @Shared
    File checksumsDir

    @Shared
    File gradlePropertiesFile

    def setup() {
        projectDir = tempDir.root

        createGroovyClass(projectDir)
        checksumsDir = new File("${projectDir}/build/checksums")

        gradlePropertiesFile = tempDir.newFile('gradle.properties')
        gradlePropertiesFile << '\nversion = 1.0'
        gradlePropertiesFile << '\nchecksum.jar=nochecksum'
    }

    def 'A build with multiple checksum tasks should contain expected tasks'() {
        when: 'standard configuration including jar and sourcesJar checksums'
            createBuildFile('''
                checksum {
                    tasks {
                        jar { }
                        sourcesJar { }
                    }
                }
            ''')
            BuildResult result = build 'tasks', '--all'

        then: 'tasks output should contain tasks for compute and saving of checksums'
            result.task(":tasks").outcome == SUCCESS
            result.output.contains(ComputeChecksumsTask.NAME)
            result.output.contains(SaveChecksumsTask.NAME)

        and: 'should also include a checksum task for each that was configured'
            result.output.contains('jarChecksum')
            result.output.contains('sourcesJarChecksum')
    }

    def 'Saving checksums creates a checksum file with a value for each archive'() {
        when:
            createBuildFile('''
                checksum {
                    tasks {
                        jar { }
                        sourcesJar { }
                    }
                }
            ''')
            File checksumsFile = new File("${projectDir}/checksums.properties")
            assert !checksumsFile.exists()
            BuildResult result = build SaveChecksumsTask.NAME
            Properties properties = loadProperties(checksumsFile)

        then: 'save checksum succeed and checksum file is created'
            result.task(":${SaveChecksumsTask.NAME}").outcome == SUCCESS
            checksumsFile.exists()

        and: 'checksum file should contain computed checksums'
            properties['checksum.jar']
            properties['checksum.sourcesJar']
    }

    def 'Checksums can be saved to a file containing other unrelated values'() {
        when:
            createBuildFile('''
                checksum {
                    propertyFile 'gradle.properties'
                    tasks {
                        jar { }
                    }
                }
            ''')
            File checksumsFile = new File("${projectDir}/gradle.properties")
            Properties properties = loadProperties(checksumsFile)
            assert properties['checksum.jar'] == 'nochecksum'
            build SaveChecksumsTask.NAME
            properties = loadProperties(checksumsFile)

        then: 'new checksum value is written'
            properties['checksum.jar']
            properties['checksum.jar'] != 'nochecksum'

        and: 'other unrelated values are not impacted'
            properties['version'] == '1.0'
    }

    @SuppressWarnings('GStringExpressionWithinString')
    def 'Plugin extension allows overriding of defaults'() {

        when: 'use extension to override propertyFile, task and property naming conventions'
            createBuildFile('''
                checksum {
                    propertyFile 'foo/bar.properties'
                    taskNameTemplate 'checksum${task.capitalize()}'
                    propertyNameTemplate '${task}.checksum'
                    tasks {
                        jar { }
                        sourcesJar {
                            propertyName 'src.checksum'
                            taskName 'checksumSrc'
                        }
                    }
                }
            ''')
            BuildResult result = build SaveChecksumsTask.NAME
            File checksumsFile = new File("${projectDir}/foo/bar.properties")

        then: 'overridden checksum task name appears in output and default checksum task name does not'
            result.output.contains(':checksumJar')
            result.output.contains(':checksumSrc')

            !result.output.contains(':jarChecksum')
            !result.output.contains(':sourcesJarChecksum')
            !result.output.contains(':checksumSourcesJar')

        and: 'checksum file exists with containing overridden property name and not the default property name'
            checksumsFile.exists()
            checksumsFile.text.contains('jar.checksum=')
            checksumsFile.text.contains('src.checksum=')

            !checksumsFile.text.contains('checksum.jar=')
            !checksumsFile.text.contains('checksum.sourcesJar=')
            !checksumsFile.text.contains('sourcesJar.checksum=')
    }

    @SuppressWarnings('GStringExpressionWithinString')
    def 'Include and exclude directives should be honored'() {

        when: 'Configure jarChecksum to exclude and sourcesJar to include only MANIFEST.MF'
            createBuildFile('''
                checksum {
                    tasks {
                        jar {
                            exclude '**/MANIFEST.MF'
                        }
                        sourcesJar {
                            include '**/MANIFEST.MF'
                        }
                    }
                }
            ''')
            build SaveChecksumsTask.NAME
            String[] jarChecksumFiles = listFilesRecursive("${checksumsDir}/jarChecksum")
            String[] sourcesChecksumFiles = listFilesRecursive("${checksumsDir}/sourcesJarChecksum")

        then: 'jarChecksum should still included classes, but NOT MANIFEST.MF'
            jarChecksumFiles.findAll { it.contains('MANIFEST.MF') }.size() == 0
            jarChecksumFiles.findAll { it.contains('Foo.class') }.size() == 1

        and: 'sourcesJarChecksum should include MANIFEST.MF, but not any source groovy files'
            sourcesChecksumFiles.findAll { it.contains('MANIFEST.MF') }.size() == 1
            sourcesChecksumFiles.findAll { it.contains('Foo.groovy') }.size() == 0


    }

    private GradleRunner createAndConfigureGradleRunner(String... arguments) {
        String[] args = arguments + '--stacktrace'
        GradleRunner.create().withProjectDir(projectDir).withArguments(args).withPluginClasspath()
    }

    private BuildResult build(String... arguments) {
        createAndConfigureGradleRunner(arguments).build()
    }

    private createBuildFile(String pluginExt) {
        buildFile = tempDir.newFile('build.gradle')

        buildFile << """
            plugins {
                id 'groovy'
                id 'com.scrain.checksum-plugin'
            }

            ${pluginExt}

            archivesBaseName='test'

            dependencies {
                compile localGroovy()
            }

            task sourcesJar(type: Jar, dependsOn: classes) {
                classifier = 'sources'
                from sourceSets.main.allSource
            }

            artifacts {
                archives sourcesJar
            }
        """
    }

    private String[] listFilesRecursive(String dirName) {
        def list = []
        new File(dirName).eachFileRecurse(FileType.FILES) {
            list << it.toString()
        }
        list
    }

    private Properties loadProperties(File file) {
        Properties props = new Properties()
        props.load(file.newReader())
        props
    }

    protected File createGroovyClass(File projectDir) {
        File groovyFile = new File("${projectDir}/src/main/groovy/com/scrain/gradle/functest/Foo.groovy")
        groovyFile.parentFile.mkdirs()
        groovyFile.createNewFile()
        groovyFile << '''
package com.scrain.gradle.functest
class Foo{}
'''
        groovyFile
    }


}
