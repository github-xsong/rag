package com.rag.model.controller;

import com.rag.common.response.R;
import com.rag.model.entity.ModelConfig;
import com.rag.model.service.ModelConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "模型管理")
@RestController
@RequestMapping("/api/models")
@RequiredArgsConstructor
public class ModelConfigController {

    private final ModelConfigService modelConfigService;

    @Operation(summary = "获取所有模型配置")
    @GetMapping
    public R<List<ModelConfig>> list() {
        return R.ok(modelConfigService.list());
    }

    @Operation(summary = "获取已启用的模型列表")
    @GetMapping("/enabled")
    public R<List<ModelConfig>> listEnabled() {
        return R.ok(modelConfigService.listEnabled());
    }

    @Operation(summary = "获取模型配置详情")
    @GetMapping("/{id}")
    public R<ModelConfig> getById(@PathVariable Long id) {
        return R.ok(modelConfigService.getById(id));
    }

    @Operation(summary = "获取默认模型")
    @GetMapping("/default")
    public R<ModelConfig> getDefault() {
        return R.ok(modelConfigService.getDefault());
    }

    @Operation(summary = "创建模型配置")
    @PostMapping
    public R<ModelConfig> create(@Valid @RequestBody ModelConfig config) {
        return R.ok(modelConfigService.create(config));
    }

    @Operation(summary = "更新模型配置")
    @PutMapping("/{id}")
    public R<ModelConfig> update(@PathVariable Long id, @Valid @RequestBody ModelConfig config) {
        return R.ok(modelConfigService.update(id, config));
    }

    @Operation(summary = "设置默认模型")
    @PutMapping("/{id}/default")
    public R<Void> setDefault(@PathVariable Long id) {
        modelConfigService.setDefault(id);
        return R.ok();
    }

    @Operation(summary = "删除模型配置")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        modelConfigService.delete(id);
        return R.ok();
    }
}
