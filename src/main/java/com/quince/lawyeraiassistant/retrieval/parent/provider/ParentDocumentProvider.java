package com.quince.lawyeraiassistant.retrieval.parent.provider;

import org.springframework.ai.document.Document;

import java.util.Collection;
import java.util.Optional;

/**
 * Parent Document Provider.
 *
 * <p>
 * 根据 parent_document_id 获取完整 Parent Document。
 *
 * <p>
 * Parent Retrieval 不关心 Parent Document
 * 存储在哪里。
 *
 * 可能来自：
 *
 * <ul>
 * <li>Memory</li>
 * <li>Redis</li>
 * <li>MySQL</li>
 * <li>ElasticSearch</li>
 * <li>Object Storage</li>
 * </ul>
 */
public interface ParentDocumentProvider {

    /**
     * 根据 Parent Document ID 查询完整 Parent。
     */
    Optional<Document> findById(
            String parentDocumentId);

    /**
     * 批量查询 Parent。
     */
    Collection<Document> findAllByIds(
            Collection<String> ids);

}