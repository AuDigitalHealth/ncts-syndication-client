package com.github.dionmcm.ncts.syndication.client;

import java.io.File;

/**
 * Class that encapsulates the information about an entry in the NCTS syndication feed
 */
public class Entry {
    private final String id;
    private final String sha256;
    private final String url;
    private final long length;
    private final String contentItemIdentifier;
    private final String contentItemVersion;
    private final String category;
    private final String categoryScheme;

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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Entry other = (Entry) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Entry [id=" + id + ", sha256=" + sha256 + ", url=" + url + ", length=" + length
                + ", contentItemIdentifier=" + contentItemIdentifier + ", contentItemVersion=" + contentItemVersion
                + ", category=" + category + ", categoryScheme=" + categoryScheme + ", file=" + file + "]";
    }

}
