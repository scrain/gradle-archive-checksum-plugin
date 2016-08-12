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

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll


class BaseExtensionSpec extends Specification{
    @Shared
    TestExtension ext = new TestExtension()

    @Unroll
    def "conversion between true/false/auto"(Object value, String expectedResult) {
        when:
            String result = ext.convertTrueFalseOtherValue(value, 'other')

        then:
            result == expectedResult

        where:
            value         |   expectedResult
            'TRUE'        |   'true'
            'False'       |   'false'
            Boolean.TRUE  |   'true'
            Boolean.FALSE |   'false'
            true          |   'true'
            false         |   'false'
            'Other'       |   'other'
    }

    def "conversion argument validation"(def arg) {
        when:
            ext.convertTrueFalseOtherValue(arg,'other')
        then:
            thrown IllegalArgumentException
        where:
            arg << [null,'']
    }


    class TestExtension extends BaseExtension { }
}