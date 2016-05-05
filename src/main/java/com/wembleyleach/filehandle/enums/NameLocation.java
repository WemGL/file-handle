package com.wembleyleach.filehandle.enums;

public enum NameLocation {
    AFTER("After name"), BEFORE("Before name");

    String rawValue;

    NameLocation(String rawValue) {
        this.rawValue = rawValue;
    }

    @Override
    public String toString() {
        return rawValue;
    }
}
