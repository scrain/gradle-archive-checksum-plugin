package com.scrain.gradle

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static com.scrain.gradle.ArchiveChecksumPlugin.*

/**
 * Created by scrain on 7/24/16.
 */
class ArchiveChecksumFunctionalSpec extends Specification {
    @Rule
    TemporaryFolder tempDir = new TemporaryFolder()

    File projectDir

    File buildFile

    File gradlePropertiesFile

    def setup() {
        projectDir = tempDir.root
        gradlePropertiesFile = tempDir.newFile('gradle.properties')
        gradlePropertiesFile << '\nversion = 1.0'
        gradlePropertiesFile << '\nchecksum.test.jar=nochecksum'
    }

    private GradleRunner createAndConfigureGradleRunner(String... arguments) {
        String[] args = arguments + '--stacktrace'
        GradleRunner.create().withProjectDir(projectDir).withArguments(args).withPluginClasspath()
    }

    private BuildResult build(String... arguments) {
        createAndConfigureGradleRunner(arguments).build()
    }

    def 'A build with multiple archive tasks should contain a checksum task for each'() {
        when:
        createBuildFile()
        BuildResult result = build 'tasks', '--all'

        then:
        result.output.contains(TASKNAME_COMPUTE_CHECKSUMS)
        result.output.contains(TASKNAME_SAVE_CHECKSUMS)
        result.output.contains('jarChecksum')
        result.output.contains('javadocJarChecksum')
        result.output.contains('sourcesJarChecksum')
    }

    def 'Saving checksums creates a checksum file with a value for each archive'() {
        when:
        createBuildFile()
        BuildResult result = build TASKNAME_SAVE_CHECKSUMS
        File checksumsFile = new File("${projectDir}/archive-checksums.properties")

        then:
        checksumsFile.exists()
        checksumsFile.text.contains 'checksum.test.jar='
        checksumsFile.text.contains 'checksum.test-javadoc.jar='
        checksumsFile.text.contains 'checksum.test-sources.jar='
    }

    def 'Checksums can be saved to a file containing other values'() {
        when:
        createBuildFile('checksumsPropertyFile="gradle.properties"')
        BuildResult result = build TASKNAME_SAVE_CHECKSUMS
        File checksumsFile = new File("${projectDir}/gradle.properties")

        then: 'exsting values are undisturbed'
        checksumsFile.text.contains 'version = 1.0'

        and: 'previous checksum value is overwritten'
        !checksumsFile.text.contains('checksum.test.jar=nochecksum')

        and: 'checksum values are written as expected'
        checksumsFile.text.contains 'checksum.test.jar='
        checksumsFile.text.contains 'checksum.test-javadoc.jar='
        checksumsFile.text.contains 'checksum.test-sources.jar='
    }

    def 'Pluging extension allows overriding of defaults'() {
        when: 'plugin extension defaults are overridden'
        createBuildFile(
                'checksumsPropertyFile="foo/bar.properties"',
                'checksumPrefix="baz."'
        )
        BuildResult result = build TASKNAME_SAVE_CHECKSUMS
        File checksumsFile = new File("${projectDir}/foo/bar.properties")

        then: 'checksum values are written as expected'
        checksumsFile.exists()
        checksumsFile.text.contains 'baz.test.jar='
        checksumsFile.text.contains 'baz.test-javadoc.jar='
        checksumsFile.text.contains 'baz.test-sources.jar='
    }

    private createBuildFile( String... pluginExt ) {
        buildFile = tempDir.newFile('build.gradle')

        buildFile << """
plugins {
    id 'groovy'
    id 'com.scrain.archive-checksum-plugin'
}

archiveChecksum {
    ${pluginExt.join('\n')}
}

archivesBaseName='test'

task sourcesJar(type: Jar, dependsOn: classes) {
    classifier = 'sources'
    from sourceSets.main.allSource
}

task javadocJar(type: Jar, dependsOn: javadoc) {
    classifier = 'javadoc'
    from javadoc.destinationDir
}

artifacts {
    archives sourcesJar
    archives javadocJar
}
"""

    }
}
