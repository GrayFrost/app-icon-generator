package org.example;

public enum IconSize {
    ICON_16(16),
    ICON_32(32),
    ICON_64(64),
    ICON_128(128),
    ICON_256(256),
    ICON_512(512),
    ICON_1024(1024);

    private final int size;

    IconSize(int size) {
        this.size = size;
    }

    public int getSize() {
        return size;
    }
} 