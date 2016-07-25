package com.scrain.gradle

import org.gradle.api.DefaultTask

/**
 * Created by scrain on 7/24/16.
 */
class BaseTask extends DefaultTask {
    ArchiveChecksumPluginExtension pluginExt

    ArchiveChecksumPluginExtension getPluginExt() {
        if ( ! pluginExt ) {
            pluginExt = project.extensions.findByName(ArchiveChecksumPlugin.PLUGIN_EXTENSION_NAME)
        }
        pluginExt
    }
}
