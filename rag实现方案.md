# RAG 项目实现方案

## 一、技术栈选型

### 1. 后端框架
- **Spring Boot 3.x** - 主框架，提供 RESTful API
- **Spring AI** - 用于集成各种 LLM 模型和向量数据库

### 2. 向量数据库
- **Milvus** 或 **Elasticsearch (with vector search)** - 存储文档向量，支持相似度检索
- 备选：**PGVector**（如果已有 PostgreSQL 基础设施）

### 3. 文档解析
- **Apache Tika** - 统一处理 PDF、Word 等多种文档格式
- **Tesseract OCR** - 图片文字识别（通过 Tika 集成）

### 4. 文本分割与向量化
- **LangChain4j** 或 **Spring AI** 内置的文本分割器
- **Embedding 模型**：OpenAI text-embedding-ada-002 / 国产模型如智谱 embedding

### 5. LLM 模型
- 支持多模型切换：OpenAI GPT、Claude、通义千问、文心一言等
- 通过配置化方式实现模型热切换

### 6. 存储
- **MySQL/PostgreSQL** - 存储知识库元数据、文件信息、用户配置
- **MinIO** - 原始文件存储（自托管，适合 Docker 部署）

### 7. 对外接口
- **Spring Security + JWT** - API 鉴权
- **OpenAPI 3.0** - 接口文档规范

---

## 二、Docker 部署方案

### 1. 容器化架构

| 服务 | 镜像 | 说明 |
|------|------|------|
| rag-app | 自建镜像 | Spring Boot 应用主体 |
| mysql | mysql:8.0 | 元数据存储 |
| milvus | milvusdb/milvus | 向量数据库 |
| minio | minio/minio | 文件对象存储 |
| redis | redis:7 | 缓存（可选） |

### 2. 部署文件结构

```
rag/
├── docker-compose.yml        # 一键启动所有服务
├── docker-compose.dev.yml    # 开发环境配置
├── .env.example              # 环境变量模板
├── Dockerfile                # 应用镜像构建
├── docs/
│   └── DEPLOY.md             # 部署说明文档
└── config/
    ├── application.yml       # 默认配置
    └── application-docker.yml # Docker 环境专用配置
```

### 3. 快速部署流程（用户视角）

```bash
# 1. 克隆项目
git clone https://github.com/xxx/rag.git

# 2. 复制并修改环境变量
cp .env.example .env
# 编辑 .env 填入 API Key 等配置

# 3. 一键启动
docker-compose up -d

# 4. 访问服务
# Web界面: http://localhost:8080
# API文档: http://localhost:8080/swagger-ui.html
```

### 4. 环境变量配置项（.env.example）

```
# 数据库配置
MYSQL_ROOT_PASSWORD=your_password
MYSQL_DATABASE=rag

# LLM 配置（按需填写）
OPENAI_API_KEY=
OPENAI_BASE_URL=
ZHIPU_API_KEY=
QWEN_API_KEY=

# MinIO 配置
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin

# 应用配置
APP_PORT=8080
DEFAULT_MODEL=openai
```

---

## 三、功能模块划分

| 模块 | 功能 |
|------|------|
| 知识库管理 | 创建/删除知识库、设置默认知识库、知识库列表 |
| 文件管理 | 上传/删除文件、文件解析、向量化入库 |
| 检索服务 | 向量相似度检索、关键词检索、混合检索 |
| 对话服务 | 多轮对话、上下文管理、流式输出 |
| 模型管理 | 模型配置、模型切换、参数调整 |
| 开放 API | 对外接口、API Key 管理、调用统计 |

---

## 四、需要优化/扩展的功能点

### 1. 检索质量优化
- 实现 **混合检索**（向量检索 + 关键词检索 + 重排序）
- 添加 **查询改写** 功能，优化用户原始问题
- 支持 **多路召回** 策略

### 2. 文档处理优化
- 智能分块策略（按段落、按语义、按固定长度）
- 文档结构识别（标题、表格、列表的特殊处理）
- 增量更新机制（文件修改后只更新变化部分）

### 3. 对话体验优化
- 支持 **流式响应**（SSE）
- 对话历史管理与上下文窗口控制
- 答案来源引用（标注来自哪个文档的哪个片段）

### 4. 多租户与权限
- 知识库级别的权限控制
- API 调用频率限制与配额管理

### 5. 可观测性
- 检索效果监控（召回率、准确率统计）
- 模型调用日志与成本统计
- 用户反馈收集（点赞/点踩）用于持续优化

### 6. 缓存策略
- 热门问题缓存
- Embedding 结果缓存，避免重复计算

---

## 五、部署友好性设计

### 1. 配置外部化
- 所有敏感配置通过环境变量注入
- 支持配置文件挂载覆盖默认配置

### 2. 健康检查
- 提供 `/actuator/health` 端点
- Docker Compose 配置 healthcheck，确保服务依赖顺序

### 3. 数据持久化
- 通过 Docker Volume 持久化数据库、向量库、文件存储
- 提供数据备份/恢复脚本

### 4. 多架构支持
- Dockerfile 支持 `linux/amd64` 和 `linux/arm64`
- 方便在不同服务器和 Mac M 系列芯片上运行

### 5. 版本管理
- 使用 GitHub Release 发布版本
- 提供 `docker-compose.yml` 中的版本标签，方便回滚

---

## 六、系统架构概览

```
                    Docker Network
┌─────────────────────────────────────────────────┐
│                                                 │
│  ┌─────────┐  ┌─────────┐  ┌─────────┐        │
│  │  MySQL  │  │ Milvus  │  │  MinIO  │        │
│  └────┬────┘  └────┬────┘  └────┬────┘        │
│       │            │            │              │
│       └────────────┼────────────┘              │
│                    │                           │
│              ┌─────┴─────┐                     │
│              │  RAG App  │ ← Spring Boot       │
│              └─────┬─────┘                     │
│                    │                           │
└────────────────────┼───────────────────────────┘
                     │
                     ▼
              ┌─────────────┐
              │   用户/API   │
              └─────────────┘
```
