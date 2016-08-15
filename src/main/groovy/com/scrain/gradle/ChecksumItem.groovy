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
import org.gradle.api.file.FileTreeElement
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.util.PatternFilterable
import org.gradle.api.tasks.util.PatternSet

/**
 * Class used as part of ChecksumExtension.tasks collection to configure a checksum to be computed for a gradle task.
 */
@ToString(ignoreNulls = true, includeNames = true)
@SuppressWarnings('ConfusingMethodName')
class ChecksumItem implements PatternFilterable {
    ChecksumItem() {

    }

    ChecksumItem(String name) {
        this.name = name
    }

    private final PatternFilterable patternSet = new PatternSet()

    /**
     * Name of task for which a checksum should be computed
     */
    String name

    /**
     * Item level override to set the checksum task source configuration.  If not set, ChecksumExtension.sourceConfig
     * will be used.
     */
    SourceConfig source

    void source(source) {
        this.source = source
    }

    /**
     * Item level override to set the task name for the checksum task that will be created
     * If not set, ChecksumExtension.taskNameTemplate will be used to generate the name automatically.
     */
    String taskName

    void taskName(taskName) {
        this.taskName = taskName
    }

    /**
     * Item level override to set the name of property under which the checksum will be stored if saved.
     * If not set, ChecksumExtension.propertyNameTemplate will be used to generate the name automatically.
     */
    String propertyName

    void propertyName(propertyName) {
        this.propertyName = propertyName
    }

    /**
     * {@inheritDoc}
     */
    @Override
    Set<String> getIncludes() {
        this.patternSet.includes
    }

    /**
     * {@inheritDoc}
     */
    @Override
    Set<String> getExcludes() {
        this.patternSet.excludes
    }

    /**
     * {@inheritDoc}
     */
    @Override
    PatternFilterable setIncludes(Iterable<String> iterable) {
        patternSet.setIncludes(iterable)
        this
    }

    /**
     * {@inheritDoc}
     */
    @Override
    PatternFilterable setExcludes(Iterable<String> iterable) {
        patternSet.setExcludes(iterable)
        this
    }

    /**
     * {@inheritDoc}
     */
    @Override
    PatternFilterable include(String... strings) {
        patternSet.include(strings)
        this
    }

    /**
     * {@inheritDoc}
     */
    @Override
    PatternFilterable include(Iterable<String> iterable) {
        patternSet.include(iterable)
        this
    }

    /**
     * {@inheritDoc}
     */
    @Override
    PatternFilterable include(Spec<FileTreeElement> spec) {
        patternSet.include(spec)
        this
    }

    /**
     * {@inheritDoc}
     */
    @Override
    PatternFilterable include(Closure closure) {
        patternSet.include(closure)
        this
    }

    /**
     * {@inheritDoc}
     */
    @Override
    PatternFilterable exclude(String... strings) {
        patternSet.exclude(strings)
        this
    }

    /**
     * {@inheritDoc}
     */
    @Override
    PatternFilterable exclude(Iterable<String> iterable) {
        patternSet.exclude(iterable)
        this
    }

    /**
     * {@inheritDoc}
     */
    @Override
    PatternFilterable exclude(Spec<FileTreeElement> spec) {
        patternSet.exclude(spec)
        this
    }

    /**
     * {@inheritDoc}
     */
    @Override
    PatternFilterable exclude(Closure closure) {
        patternSet.exclude(closure)
        this
    }
}
