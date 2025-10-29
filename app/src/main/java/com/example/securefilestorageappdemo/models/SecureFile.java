package com.example.securefilestorageappdemo.models;

import java.io.File;

public class SecureFile {
    private String fileName;
    private String filePath;
    private boolean isImage;
    private boolean isDirectory;

    public SecureFile(String fileName, String filePath, boolean isImage) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.isImage = isImage;
        this.isDirectory = new File(filePath).isDirectory();
    }

    public SecureFile(String fileName, String filePath, boolean isImage, boolean isDirectory) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.isImage = isImage;
        this.isDirectory = isDirectory;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public boolean isImage() {
        return isImage && !isDirectory;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
        this.isDirectory = new File(filePath).isDirectory();
    }

    public void setIsImage(boolean isImage) {
        this.isImage = isImage;
    }

    public void setIsDirectory(boolean isDirectory) {
        this.isDirectory = isDirectory;
    }
}