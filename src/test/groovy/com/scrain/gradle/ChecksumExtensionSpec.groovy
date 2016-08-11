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

import spock.lang.Specification
import spock.lang.Unroll

class ChecksumExtensionSpec extends Specification {
    def "setting invalid checksum algorithm should throw exception"() {
        ChecksumExtension ext = new ChecksumExtension()

        when:
            ext.algorithm = ChecksumExtension.ALG_MD5
            ext.algorithm = ChecksumExtension.ALG_SHA1

        then:
            notThrown IllegalArgumentException

        when:
            ext.algorithm = 'foo'

        then:
            thrown IllegalArgumentException

    }

    @Unroll
    @SuppressWarnings('GStringExpressionWithinString')
    def "Checksum task template can be overridden"(ChecksumExtension ext, ChecksumItem item, String expectedName) {
        expect:
            ext.checksumTaskName(item) == expectedName

        where:
            ext                                               | item                              | expectedName
            new ChecksumExtension()                           | new ChecksumItem('foo')           | 'fooChecksum'
            new ChecksumExtension(taskNameTemplate: 'x$name') | new ChecksumItem('foo')           | 'xfoo'
            new ChecksumExtension()                           | new ChecksumItem(taskName: 'bar') | 'bar'
            new ChecksumExtension(taskNameTemplate: 'x$name') | new ChecksumItem(taskName: 'bar') | 'bar'
    }

    @Unroll
    @SuppressWarnings('GStringExpressionWithinString')
    def "Checksum property template can be overridden"(ChecksumExtension ext, ChecksumItem item, String expectedName) {
        expect:
            ext.checksumPropertyName(item) == expectedName

        where:
            ext                                                     | item                                  | expectedName
            new ChecksumExtension()                                 | new ChecksumItem('foo')               | 'checksum.foo'
            new ChecksumExtension(propertyNameTemplate: 'x${name}') | new ChecksumItem('foo')               | 'xfoo'
            new ChecksumExtension()                                 | new ChecksumItem(propertyName: 'bar') | 'bar'
            new ChecksumExtension(propertyNameTemplate: 'x${name}') | new ChecksumItem(propertyName: 'bar') | 'bar'
    }
}
