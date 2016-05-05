package com.wembleyleach.filehandle.enums;

public enum RenameOption {
    REPLACE("Replace Text"), ADD("Add Text"), FORMAT("Format");

    private String rawValue;

    RenameOption(String rawValue) {
        this.rawValue = rawValue;
    }

    @Override
    public String toString() {
        return rawValue;
    }
}
