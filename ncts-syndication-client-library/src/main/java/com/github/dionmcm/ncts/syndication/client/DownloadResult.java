package com.github.dionmcm.ncts.syndication.client;

import java.io.File;

/**
 * Result of a download which indicates where the file was downloaded to and whether it was freshly downloaded (either
 * it wasn't already downloaded or the local file's hash didn't match) or that the local directory already had the
 * requested artefact with a hash matching the feed.
 */
public class DownloadResult {

    private File file;
    private boolean freshlyDownloaded;

    public DownloadResult(File file, boolean downloaded) {
        this.file = file;
        this.freshlyDownloaded = downloaded;
    }

    /**
     * @return location of the file
     */
    public File getFile() {
        return file;
    }

    /**
     * @return true if the file was downloaded or false if the local location already had an up to date copy of the
     *         artefact with a hash matching the feed
     */
    public boolean isFreshlyDownloaded() {
        return freshlyDownloaded;
    }

}
