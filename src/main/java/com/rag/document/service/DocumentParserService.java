package com.rag.document.service;

import com.rag.common.enums.FileType;
import com.rag.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Service
public class DocumentParserService {

    private final Tika tika = new Tika();

    public String parse(InputStream inputStream, String fileType) {
        try {
            FileType type = FileType.fromExtension(fileType);

            if (type.isImage()) {
                return parseImage(inputStream);
            }

            return tika.parseToString(inputStream);
        } catch (IOException | TikaException e) {
            throw new BusinessException("文档解析失败: " + e.getMessage());
        }
    }

    private String parseImage(InputStream inputStream) {
        try {
            return tika.parseToString(inputStream);
        } catch (IOException | TikaException e) {
            log.warn("图片OCR解析失败，可能需要安装Tesseract: {}", e.getMessage());
            throw new BusinessException("图片解析失败，请确保已安装OCR引擎");
        }
    }
}
