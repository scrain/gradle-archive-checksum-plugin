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

import groovy.transform.ToString


/**
 * Class used as part of ChecksumExtension.tasks collection to configure a checksum to be computed for a gradle task.
 */
@ToString
class ChecksumItem {
    ChecksumItem() { }

    ChecksumItem(String name) {
        this.name = name
    }

    /**
     * Name of task for which a checksum should be computed
     */
    String name

    /**
     *  boolean indicating if the task's source should be used to compute the checksum (default).  If useSource
     *  is false, then the task's output is used.
     */
    boolean useSource = true

    /**
     * Name to use for the checksum task that will be created
     */
    String taskName

    /**
     * Name to use for the checksum task that will be created
     */
    String propertyName
}
