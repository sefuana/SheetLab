package com.axsaafe.sheetlab.model;

import java.io.File;

public class SheetFile {
    private String name;
    private String path;
    private long lastModified;
    private boolean isHighlighted;

    public SheetFile(String name, String path, long lastModified) {
        this.name = name;
        this.path = path;
        this.lastModified = lastModified;
        this.isHighlighted = false;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public long getLastModified() { return lastModified; }
    public boolean isHighlighted() { return isHighlighted; }
    public void setHighlighted(boolean highlighted) { isHighlighted = highlighted; }

    public String getDisplayName() {
        if (name.toLowerCase().endsWith(".xlsx")) {
            return name.substring(0, name.length() - 5);
        }
        return name;
    }

    public File toFile() { return new File(path); }
}
