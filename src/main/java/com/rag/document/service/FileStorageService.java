package com.rag.document.service;

import com.rag.common.exception.BusinessException;
import com.rag.config.MinioConfig;
import io.minio.*;
import io.minio.errors.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;

    @PostConstruct
    public void init() {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(minioConfig.getBucketName()).build());
            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(minioConfig.getBucketName()).build());
                log.info("创建存储桶: {}", minioConfig.getBucketName());
            }
        } catch (Exception e) {
            log.error("初始化MinIO存储桶失败", e);
        }
    }

    public String upload(MultipartFile file, Long knowledgeBaseId) {
        try {
            String extension = getFileExtension(file.getOriginalFilename());
            String objectName = knowledgeBaseId + "/" + UUID.randomUUID() + "." + extension;

            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());

            log.info("文件上传成功: {}", objectName);
            return objectName;
        } catch (Exception e) {
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }
    }

    public InputStream download(String objectName) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            throw new BusinessException("文件下载失败: " + e.getMessage());
        }
    }

    public void delete(String objectName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectName)
                    .build());
            log.info("文件删除成功: {}", objectName);
        } catch (Exception e) {
            throw new BusinessException("文件删除失败: " + e.getMessage());
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }
}
