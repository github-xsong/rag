package com.rag.document.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class VectorStoreService {

    private final VectorStore vectorStore;

    /**
     * 将文本分块后存入向量数据库
     *
     * @param content        文档文本内容
     * @param knowledgeBaseId 知识库ID
     * @param documentId      文档ID
     * @param fileName        文件名
     * @return 分块数量
     */
    public int storeDocument(String content, Long knowledgeBaseId, Long documentId, String fileName) {
        TokenTextSplitter splitter = new TokenTextSplitter(800, 200, 5, 10000, true);

        Document aiDocument = new Document(content, Map.of(
                "knowledgeBaseId", knowledgeBaseId.toString(),
                "documentId", documentId.toString(),
                "fileName", fileName
        ));

        List<Document> chunks = splitter.apply(List.of(aiDocument));

        vectorStore.add(chunks);
        log.info("文档 [{}] 分块存储完成，共 {} 个分块", fileName, chunks.size());
        return chunks.size();
    }

    public List<Document> search(String query, Long knowledgeBaseId, int topK) {
        SearchRequest request = SearchRequest.from(query)
                .withTopK(topK)
                .withFilterExpression("knowledgeBaseId == '" + knowledgeBaseId + "'");

        return vectorStore.similaritySearch(request);
    }

    public void deleteByDocumentId(Long documentId) {
        vectorStore.delete(List.of("documentId == '" + documentId + "'"));
        log.info("已删除文档 [{}] 的向量数据", documentId);
    }
}
