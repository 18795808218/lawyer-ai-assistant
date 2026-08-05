package com.quince.lawyeraiassistant.retrieval.parent.provider;

import org.springframework.ai.document.Document;

import java.util.Collection;

/**
 * Parent Document 写入接口。
 *
 * <p>
 * 文档 ETL 通过本接口保存尚未切分的 Parent Document。
 * 检索阶段则通过 {@link ParentDocumentProvider} 读取。
 * </p>
 */
public interface ParentDocumentStore {

    /**
     * 保存单个 Parent Document。
     *
     * @param document Parent Document
     */
    void save(Document document);

    /**
     * 批量保存 Parent Document。
     *
     * @param documents Parent Document 集合
     */
    void saveAll(Collection<Document> documents);
}