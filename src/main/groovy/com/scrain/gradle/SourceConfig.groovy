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

import org.gradle.api.Task

enum SourceConfig {
    AUTO(
        'include task inputs if task has them, otherwise include task outputs',
        { Task task -> task.inputs.hasInputs },
        { Task task -> !task.inputs.hasInputs }
    ),
    INPUTS(
        'include task inputs only',
        { Task task -> true },
        { Task task -> false }
    ),
    OUTPUTS(
        'include task outputs only',
        { Task task -> false },
        { Task task -> true }
    ),
    BOTH(
        'include both task inputs and outputs',
        { Task task -> true },
        { Task task -> true }
    )

    final String description
    final Closure<Boolean> includeInputs
    final Closure<Boolean> includeOutputs

    private SourceConfig(String desc, Closure<Boolean> includeInputs, Closure<Boolean> includeOutputs) {
        this.description = desc
        this.includeInputs = includeInputs
        this.includeOutputs = includeOutputs
    }
}