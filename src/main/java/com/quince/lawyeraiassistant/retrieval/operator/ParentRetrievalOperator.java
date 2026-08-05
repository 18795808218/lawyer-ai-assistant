package com.quince.lawyeraiassistant.retrieval.operator;

import com.quince.lawyeraiassistant.retrieval.model.RetrieverContext;
import com.quince.lawyeraiassistant.retrieval.parent.provider.ParentDocumentProvider;
import org.springframework.ai.document.Document;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Parent Retrieval 节点。
 *
 * <p>
 * 根据 Child Chunk metadata 中的 parent_document_id，
 * 批量查询对应的 Parent Document。
 * </p>
 *
 * <p>
 * 执行流程：
 * </p>
 *
 * <pre>
 * Child Chunks
 *      ↓
 * 提取 parent_document_id
 *      ↓
 * 去重并保留原始顺序
 *      ↓
 * ParentDocumentProvider
 *      ↓
 * Parent Documents
 *      ↓
 * 新的 RetrieverContext
 * </pre>
 */
@Component
@Order(200)
public class ParentRetrievalOperator
        implements RetrievalOperator {

    private static final String PARENT_DOCUMENT_ID = "parent_document_id";

    private final ParentDocumentProvider parentDocumentProvider;

    public ParentRetrievalOperator(
            ParentDocumentProvider parentDocumentProvider) {

        this.parentDocumentProvider = Objects.requireNonNull(
                parentDocumentProvider,
                "parentDocumentProvider must not be null");
    }

    @Override
    public RetrieverContext retrieve(
            RetrieverContext context) {

        Objects.requireNonNull(
                context,
                "RetrieverContext must not be null");

        /*
         * RetrieverContext 已保证 documents 不为 null，
         * 因此这里只需判断是否为空。
         */
        if (!context.hasDocuments()) {
            return context;
        }

        Set<String> parentDocumentIds = extractParentDocumentIds(
                context.getDocuments());

        /*
         * 没有任何 Chunk 包含 parent_document_id 时，
         * 保留原始 Chunk，不清空结果。
         */
        if (parentDocumentIds.isEmpty()) {
            return context;
        }

        Collection<Document> parentDocuments = parentDocumentProvider.findAllByIds(parentDocumentIds);

        /*
         * Provider 找不到 Parent 时，
         * 回退到原始 Chunk，避免知识丢失。
         */
        if (parentDocuments == null
                || parentDocuments.isEmpty()) {
            return context;
        }

        return context.toBuilder()
                .documents(
                        List.copyOf(parentDocuments))
                .build();
    }

    private Set<String> extractParentDocumentIds(
            List<Document> documents) {

        Set<String> parentDocumentIds = new LinkedHashSet<>();

        for (Document document : documents) {
            if (document == null) {
                continue;
            }

            Object rawParentId = document.getMetadata()
                    .get(PARENT_DOCUMENT_ID);

            if (rawParentId == null) {
                continue;
            }

            String parentId = rawParentId.toString().trim();

            if (!parentId.isEmpty()) {
                parentDocumentIds.add(
                        parentId);
            }
        }

        return parentDocumentIds;
    }
}