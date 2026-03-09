package com.rag.knowledge.controller;

import com.rag.common.response.R;
import com.rag.knowledge.entity.KnowledgeBase;
import com.rag.knowledge.service.KnowledgeBaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "知识库管理")
@RestController
@RequestMapping("/api/knowledge-base")
@RequiredArgsConstructor
public class KnowledgeBaseController {

    private final KnowledgeBaseService knowledgeBaseService;

    @Operation(summary = "获取知识库列表")
    @GetMapping
    public R<List<KnowledgeBase>> list() {
        return R.ok(knowledgeBaseService.list());
    }

    @Operation(summary = "获取知识库详情")
    @GetMapping("/{id}")
    public R<KnowledgeBase> getById(@PathVariable Long id) {
        return R.ok(knowledgeBaseService.getById(id));
    }

    @Operation(summary = "获取默认知识库")
    @GetMapping("/default")
    public R<KnowledgeBase> getDefault() {
        return R.ok(knowledgeBaseService.getDefault());
    }

    @Operation(summary = "创建知识库")
    @PostMapping
    public R<KnowledgeBase> create(@Valid @RequestBody KnowledgeBase knowledgeBase) {
        return R.ok(knowledgeBaseService.create(knowledgeBase));
    }

    @Operation(summary = "更新知识库")
    @PutMapping("/{id}")
    public R<KnowledgeBase> update(@PathVariable Long id, @Valid @RequestBody KnowledgeBase knowledgeBase) {
        return R.ok(knowledgeBaseService.update(id, knowledgeBase));
    }

    @Operation(summary = "设置默认知识库")
    @PutMapping("/{id}/default")
    public R<Void> setDefault(@PathVariable Long id) {
        knowledgeBaseService.setDefault(id);
        return R.ok();
    }

    @Operation(summary = "删除知识库")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        knowledgeBaseService.delete(id);
        return R.ok();
    }
}
