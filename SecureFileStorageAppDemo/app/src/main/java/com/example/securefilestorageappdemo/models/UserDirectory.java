package com.example.securefilestorageappdemo.models;

public class UserDirectory {
    private String name;
    private String path;

    public UserDirectory(String name, String path) {
        this.name = name;
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return name;
    }
}
