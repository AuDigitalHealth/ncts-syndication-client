package com.github.dionmcm.ncts.syndication.client;

import java.io.File;

public class HashValidationFailureException extends Exception {
    private static final long serialVersionUID = 1L;

    File filePath;
    String hash;
    long length;
    String feedHash;
    long feedLength;

    public HashValidationFailureException(File filePath, String hash, long length, String feedHash, long feedLength) {
        super("File " + filePath.getAbsolutePath() + " with hash " + hash + " and length " + length
                + " once downloaded did not match the advertised hash " + feedHash + " and/or length "
                + feedLength + ". Downloaded file deleted, process aborted.");
        this.filePath = filePath;
        this.hash = hash;
        this.length = length;
        this.feedHash = feedHash;
        this.feedLength = feedLength;
    }

}
