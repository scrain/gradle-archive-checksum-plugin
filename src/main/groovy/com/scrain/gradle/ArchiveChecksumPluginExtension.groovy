package com.scrain.gradle

/**
 * Created by scrain on 7/22/16.
 */
class ArchiveChecksumPluginExtension {
    String checksumsPropertyFile="archive-checksums.properties"

    String checksumPrefix="checksum."

    Map<String, String> checksums = [:]
}
