CREATE DATABASE IF NOT EXISTS rag DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE rag;

-- 知识库表
CREATE TABLE IF NOT EXISTS knowledge_base (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '知识库名称',
    description VARCHAR(500) COMMENT '知识库描述',
    is_default TINYINT(1) DEFAULT 0 COMMENT '是否默认知识库',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_name (name)
) ENGINE=InnoDB COMMENT='知识库';

-- 文档表
CREATE TABLE IF NOT EXISTS document (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    knowledge_base_id BIGINT NOT NULL COMMENT '所属知识库ID',
    file_name VARCHAR(255) NOT NULL COMMENT '文件名',
    file_type VARCHAR(20) NOT NULL COMMENT '文件类型: PDF, IMAGE, WORD',
    file_path VARCHAR(500) NOT NULL COMMENT 'MinIO存储路径',
    file_size BIGINT DEFAULT 0 COMMENT '文件大小(字节)',
    chunk_count INT DEFAULT 0 COMMENT '分块数量',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态: PENDING, PROCESSING, COMPLETED, FAILED',
    error_message VARCHAR(1000) COMMENT '错误信息',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_knowledge_base_id (knowledge_base_id),
    CONSTRAINT fk_doc_kb FOREIGN KEY (knowledge_base_id) REFERENCES knowledge_base(id)
) ENGINE=InnoDB COMMENT='文档';

-- 对话会话表
CREATE TABLE IF NOT EXISTS chat_session (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    knowledge_base_id BIGINT NOT NULL COMMENT '关联知识库ID',
    title VARCHAR(200) DEFAULT '新对话' COMMENT '会话标题',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_kb_id (knowledge_base_id),
    CONSTRAINT fk_session_kb FOREIGN KEY (knowledge_base_id) REFERENCES knowledge_base(id)
) ENGINE=InnoDB COMMENT='对话会话';

-- 对话消息表
CREATE TABLE IF NOT EXISTS chat_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id BIGINT NOT NULL COMMENT '所属会话ID',
    role VARCHAR(20) NOT NULL COMMENT '角色: USER, ASSISTANT',
    content TEXT NOT NULL COMMENT '消息内容',
    sources JSON COMMENT '引用来源',
    model_name VARCHAR(50) COMMENT '使用的模型名称',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_session_id (session_id),
    CONSTRAINT fk_msg_session FOREIGN KEY (session_id) REFERENCES chat_session(id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='对话消息';

-- 模型配置表
CREATE TABLE IF NOT EXISTS model_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL COMMENT '配置名称',
    provider VARCHAR(30) NOT NULL COMMENT '提供商: OPENAI, CLAUDE, QWEN, ZHIPU',
    model_name VARCHAR(50) NOT NULL COMMENT '模型名称',
    api_key VARCHAR(500) COMMENT 'API密钥',
    base_url VARCHAR(300) COMMENT 'API地址',
    is_default TINYINT(1) DEFAULT 0 COMMENT '是否默认模型',
    enabled TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_name (name)
) ENGINE=InnoDB COMMENT='模型配置';

-- API Key 表
CREATE TABLE IF NOT EXISTS api_key (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '名称',
    api_key VARCHAR(64) NOT NULL COMMENT 'API Key',
    knowledge_base_id BIGINT COMMENT '绑定知识库ID（为空则可访问所有）',
    enabled TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    last_used_at DATETIME COMMENT '最后使用时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_api_key (api_key),
    CONSTRAINT fk_apikey_kb FOREIGN KEY (knowledge_base_id) REFERENCES knowledge_base(id)
) ENGINE=InnoDB COMMENT='开放API密钥';
