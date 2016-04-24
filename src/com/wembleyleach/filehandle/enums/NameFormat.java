package com.wembleyleach.filehandle.enums;

public enum NameFormat {
    INDEX("Name and Index"), COUNTER("Name and Counter"), DATE("Name and Date");

    String rawValue;

    NameFormat(String rawValue) {
        this.rawValue = rawValue;
    }

    @Override
    public String toString() {
        return rawValue;
    }
}
