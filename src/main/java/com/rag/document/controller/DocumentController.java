package com.rag.document.controller;

import com.rag.common.response.R;
import com.rag.document.entity.Document;
import com.rag.document.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "文档管理")
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @Operation(summary = "获取知识库下的文档列表")
    @GetMapping("/knowledge-base/{knowledgeBaseId}")
    public R<List<Document>> list(@PathVariable Long knowledgeBaseId) {
        return R.ok(documentService.listByKnowledgeBase(knowledgeBaseId));
    }

    @Operation(summary = "上传文档到知识库")
    @PostMapping("/knowledge-base/{knowledgeBaseId}/upload")
    public R<Document> upload(@PathVariable Long knowledgeBaseId, @RequestParam("file") MultipartFile file) {
        return R.ok(documentService.upload(knowledgeBaseId, file));
    }

    @Operation(summary = "删除文档")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        documentService.delete(id);
        return R.ok();
    }
}
