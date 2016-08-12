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

/**
 * Base class for extensions configuration extensions.
 * Contains convenience methods for handling user configured values.
 */
abstract class BaseExtension {

    /**
     * Coerces provided value into one of three returned String values:
     * 1 - 'true', 2 - 'false' or 3 - the provided other value.
     *
     * If the value is equal to provided other value, then other is returned.
     * Otherwise, the value is coerced into either a 'true' or 'false' string value.
     *
     * @param value Object being coerced
     * @param other String considered as possible return value.
     * @return other String if value is equal, otherwise 'true' or 'false'
     */
    String convertTrueFalseOtherValue(Object value, String other) {
        if ( value == null || '' == value) {
            throw new IllegalArgumentException('value required')
        }

        if ( other.equalsIgnoreCase(value.toString()) ) {
            other
        } else {
            value.toBoolean().toString()
        }
    }
}
