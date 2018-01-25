package com.github.dionmcm.ncts.syndication.client;

import java.io.File;

import org.apache.commons.lang3.builder.EqualsBuilder;

/**
 * Class that encapsulates the information about an entry in the NCTS syndication feed
 */
public class Entry {
    private String id;
    private String sha256;
    private String url;
    private long length;
    private String contentItemIdentifier;
    private String contentItemVersion;
    private String category;
    private String categoryScheme;

    private File file;

    public Entry(String id, String sha256, String url, long length, String contentItemIdentifier,
            String contentItemVersion, String category, String categoryScheme) {
        this.id = id;
        this.sha256 = sha256;
        this.url = url;
        this.length = length;
        this.contentItemIdentifier = contentItemIdentifier;
        this.contentItemVersion = contentItemVersion;
        this.category = category;
        this.categoryScheme = categoryScheme;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        Entry rhs = (Entry) obj;
        return new EqualsBuilder()
            .appendSuper(super.equals(obj))
            .append(id, rhs.id)
            .isEquals();
    }

    public String getId() {
        return id;
    }

    public String getSha256() {
        return sha256;
    }

    public String getUrl() {
        return url;
    }

    public long getLength() {
        return length;
    }

    public String getContentItemIdentifier() {
        return contentItemIdentifier;
    }

    public String getContentItemVersion() {
        return contentItemVersion;
    }

    public String getCategory() {
        return category;
    }

    public String getCategoryScheme() {
        return categoryScheme;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

}
