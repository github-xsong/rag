package com.rag.document.service;

import com.rag.common.enums.DocumentStatus;
import com.rag.common.exception.BusinessException;
import com.rag.document.entity.Document;
import com.rag.document.repository.DocumentRepository;
import com.rag.knowledge.service.KnowledgeBaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of("pdf", "doc", "docx", "png", "jpg", "jpeg");

    private final DocumentRepository documentRepository;
    private final KnowledgeBaseService knowledgeBaseService;
    private final FileStorageService fileStorageService;
    private final DocumentParserService documentParserService;
    private final VectorStoreService vectorStoreService;

    public List<Document> listByKnowledgeBase(Long knowledgeBaseId) {
        knowledgeBaseService.getById(knowledgeBaseId);
        return documentRepository.findByKnowledgeBaseIdOrderByCreatedAtDesc(knowledgeBaseId);
    }

    @Transactional
    public Document upload(Long knowledgeBaseId, MultipartFile file) {
        knowledgeBaseService.getById(knowledgeBaseId);

        String extension = getFileExtension(file.getOriginalFilename());
        if (!SUPPORTED_EXTENSIONS.contains(extension)) {
            throw new BusinessException("不支持的文件类型: " + extension + "，支持: " + SUPPORTED_EXTENSIONS);
        }

        String filePath = fileStorageService.upload(file, knowledgeBaseId);

        Document document = new Document();
        document.setKnowledgeBaseId(knowledgeBaseId);
        document.setFileName(file.getOriginalFilename());
        document.setFileType(extension);
        document.setFilePath(filePath);
        document.setFileSize(file.getSize());
        document.setStatus(DocumentStatus.PENDING);

        document = documentRepository.save(document);

        processDocumentAsync(document);

        return document;
    }

    @Async("documentProcessExecutor")
    public void processDocumentAsync(Document document) {
        try {
            document.setStatus(DocumentStatus.PROCESSING);
            documentRepository.save(document);

            InputStream inputStream = fileStorageService.download(document.getFilePath());
            String content = documentParserService.parse(inputStream, document.getFileType());

            int chunkCount = vectorStoreService.storeDocument(
                    content, document.getKnowledgeBaseId(), document.getId(), document.getFileName());

            document.setChunkCount(chunkCount);
            document.setStatus(DocumentStatus.COMPLETED);
            documentRepository.save(document);

            log.info("文档处理完成: {} ({}个分块)", document.getFileName(), chunkCount);
        } catch (Exception e) {
            log.error("文档处理失败: {}", document.getFileName(), e);
            document.setStatus(DocumentStatus.FAILED);
            document.setErrorMessage(e.getMessage());
            documentRepository.save(document);
        }
    }

    @Transactional
    public void delete(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("文档不存在: " + id));

        fileStorageService.delete(document.getFilePath());
        vectorStoreService.deleteByDocumentId(id);
        documentRepository.deleteById(id);

        log.info("文档已删除: {}", document.getFileName());
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }
}
