# RAG 知识库问答系统

基于 Spring Boot + Spring AI 的 RAG（Retrieval-Augmented Generation）知识库智能问答系统。支持文档上传、自动向量化、多模型对话，并提供开放 API 供外部调用。

## 功能特性

- **知识库管理** - 创建/删除知识库，设置默认知识库
- **文档管理** - 上传/删除文件，支持 PDF、Word、图片（OCR）
- **AI 问答** - 基于知识库的智能对话，自动检索相关文档片段
- **多模型支持** - 可配置多个 LLM 模型，支持自由切换
- **开放 API** - 通过 API Key 对外提供问答接口
- **Docker 部署** - 一键启动所有依赖服务

## 技术栈

| 组件 | 技术 |
|------|------|
| 前端 | Vue 3 + Vite + Element Plus |
| 后端框架 | Spring Boot 3.3 |
| AI 集成 | Spring AI 1.0 |
| 向量数据库 | Milvus 2.4 |
| 关系数据库 | MySQL 8.0 |
| 文件存储 | MinIO |
| 文档解析 | Apache Tika + Tesseract OCR |
| 缓存 | Redis 7 |
| API 文档 | SpringDoc OpenAPI (Swagger) |

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- Node.js 18+
- Docker & Docker Compose
- 一个可用的 LLM API Key（OpenAI 或兼容接口）

### 部署步骤

```bash
# 1. 克隆项目
git clone https://github.com/github-xsong/rag.git
cd rag

# 2. 配置环境变量
cp .env.example .env
# 编辑 .env，填入你的 API Key 等配置

# 3. 构建后端
mvn clean package -DskipTests

# 4. 构建前端
cd frontend
npm install
npm run build
cd ..

# 5. 启动服务
docker compose up -d

# 6. 查看服务状态
docker compose ps
```

启动完成后访问：

- 前端页面：http://localhost
- API 文档：http://localhost:8080/swagger-ui.html
- MinIO 控制台：http://localhost:9001（默认账号 minioadmin/minioadmin）

## 配置说明

编辑 `.env` 文件进行配置：

```properties
# LLM API 配置
OPENAI_API_KEY=sk-xxx          # 你的 API Key
OPENAI_BASE_URL=https://api.openai.com  # API 地址（支持第三方兼容接口）

# 数据库
MYSQL_ROOT_PASSWORD=rag123456

# MinIO 文件存储
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin
```

## API 接口概览

### 知识库管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/knowledge-base` | 获取知识库列表 |
| POST | `/api/knowledge-base` | 创建知识库 |
| PUT | `/api/knowledge-base/{id}` | 更新知识库 |
| PUT | `/api/knowledge-base/{id}/default` | 设为默认 |
| DELETE | `/api/knowledge-base/{id}` | 删除知识库 |

### 文档管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/documents/knowledge-base/{id}` | 获取文档列表 |
| POST | `/api/documents/knowledge-base/{id}/upload` | 上传文档 |
| DELETE | `/api/documents/{id}` | 删除文档 |

### AI 对话

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/chat` | 发送消息 |
| GET | `/api/chat/sessions` | 获取会话列表 |
| GET | `/api/chat/sessions/{id}/messages` | 获取消息记录 |
| DELETE | `/api/chat/sessions/{id}` | 删除会话 |

### 模型管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/models` | 获取模型列表 |
| POST | `/api/models` | 添加模型配置 |
| PUT | `/api/models/{id}/default` | 设为默认模型 |

### 开放 API

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/open/chat` | 外部调用问答（需 `X-API-Key` 请求头） |
| POST | `/api/api-keys` | 创建 API Key |

## 项目结构

```
rag/
├── frontend/                # 前端（Vue 3 + Vite + Element Plus）
│   ├── src/views/chat/      # AI 对话页面
│   ├── src/views/knowledge/ # 知识库管理
│   ├── src/views/document/  # 文档管理
│   ├── src/views/model/     # 模型配置
│   └── src/views/apikey/    # API Key 管理
├── src/main/java/com/rag/   # 后端（Spring Boot）
│   ├── config/              # 配置类
│   ├── common/              # 通用组件
│   ├── knowledge/           # 知识库模块
│   ├── document/            # 文档模块
│   ├── chat/                # 对话模块（RAG 核心）
│   ├── model/               # 模型管理模块
│   └── openapi/             # 开放 API 模块
├── docker-compose.yml       # 一键部署编排
├── Dockerfile               # 后端运行镜像
└── .env.example             # 环境变量模板
```

## 本地开发

如需本地开发调试：

```bash
# 1. 启动中间件
docker-compose up -d mysql milvus minio redis

# 2. 启动后端：在 IDE 中运行 RagApplication.java

# 3. 启动前端
cd frontend
npm install
npm run dev
# 访问 http://localhost:3000
```

前端开发服务器已配置 API 代理，会自动将 `/api` 请求转发到后端 `localhost:8080`。

## License

MIT
