package com.rag.common.enums;

import lombok.Getter;

@Getter
public enum FileType {

    PDF("pdf", "application/pdf"),
    WORD("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
    DOC("doc", "application/msword"),
    IMAGE_PNG("png", "image/png"),
    IMAGE_JPG("jpg", "image/jpeg"),
    IMAGE_JPEG("jpeg", "image/jpeg");

    private final String extension;
    private final String mimeType;

    FileType(String extension, String mimeType) {
        this.extension = extension;
        this.mimeType = mimeType;
    }

    public static FileType fromExtension(String extension) {
        for (FileType type : values()) {
            if (type.extension.equalsIgnoreCase(extension)) {
                return type;
            }
        }
        throw new IllegalArgumentException("不支持的文件类型: " + extension);
    }

    public boolean isImage() {
        return this == IMAGE_PNG || this == IMAGE_JPG || this == IMAGE_JPEG;
    }
}
