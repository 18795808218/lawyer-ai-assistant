package com.quince.lawyeraiassistant.agent.tool.legal;

import com.quince.lawyeraiassistant.agent.model.ToolAction;
import com.quince.lawyeraiassistant.agent.model.ToolExecutionResult;
import com.quince.lawyeraiassistant.agent.tool.AgentTool;
import com.quince.lawyeraiassistant.retrieval.model.RetrieverContext;
import com.quince.lawyeraiassistant.retrieval.orchestration.RetrievalOrchestrator;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 法律知识检索 Tool。
 *
 * <p>
 * 将 Agent Runtime 中的法律知识检索 Action
 * 适配到项目现有 RetrievalOrchestrator。
 * </p>
 *
 * <pre>
 * ToolAction
 *      ↓
 * LegalKnowledgeTool
 *      ↓
 * RetrievalOrchestrator
 *      ↓
 * QueryPipeline
 *      ↓
 * RetrieverPipeline
 *      ↓
 * RetrieverContext
 *      ↓
 * ToolExecutionResult
 * </pre>
 *
 * <p>
 * 本类只作为 Agent Tool Adapter，
 * 不直接实现向量检索、Query Rewrite、
 * Parent Retrieval 或 ReRank 等业务逻辑。
 * </p>
 */
@Component
public class LegalKnowledgeTool
        implements AgentTool {

    /**
     * Tool 在 Agent Runtime 中的唯一名称。
     */
    public static final String TOOL_NAME = "searchLegalKnowledge";

    /**
     * ToolAction.arguments 中法律问题参数的 Key。
     */
    public static final String LEGAL_QUESTION_ARGUMENT = "legalQuestion";

    private static final String NO_KNOWLEDGE_FOUND = "未检索到与当前法律问题相关的知识。";

    private final RetrievalOrchestrator retrievalOrchestrator;

    public LegalKnowledgeTool(
            RetrievalOrchestrator retrievalOrchestrator) {

        this.retrievalOrchestrator = Objects.requireNonNull(
                retrievalOrchestrator,
                "retrievalOrchestrator must not be null");
    }

    /**
     * 返回 Tool 唯一名称。
     */
    @Override
    public String name() {
        return TOOL_NAME;
    }

    /**
     * 执行法律知识检索。
     *
     * <p>
     * 参数要求：
     * </p>
     *
     * <pre>
     * {
     *     "legalQuestion": "劳动合同违法解除的法律责任"
     * }
     * </pre>
     *
     * @param action 当前 Tool Action
     * @return Tool 执行结果
     */
    @Override
    public ToolExecutionResult execute(
            ToolAction action) {

        Objects.requireNonNull(
                action,
                "ToolAction must not be null");

        validateToolName(action);

        String legalQuestion = extractLegalQuestion(
                action.getArguments());

        try {
            RetrieverContext retrievalContext = retrievalOrchestrator.retrieve(
                    legalQuestion);

            Objects.requireNonNull(
                    retrievalContext,
                    "RetrievalOrchestrator must not return null");

            return ToolExecutionResult.success(
                    formatRetrievalResult(
                            retrievalContext));
        } catch (RuntimeException exception) {
            return ToolExecutionResult.failure(
                    resolveErrorMessage(
                            exception));
        }
    }

    /**
     * 防止 Runtime 将属于其他 Tool 的 Action
     * 错误路由到当前 Tool。
     */
    private void validateToolName(
            ToolAction action) {

        if (!TOOL_NAME.equals(
                action.getToolName())) {

            throw new IllegalArgumentException(
                    "ToolAction is not intended for "
                            + TOOL_NAME
                            + ": "
                            + action.getToolName());
        }
    }

    /**
     * 从 Action 参数中提取法律问题。
     */
    private String extractLegalQuestion(
            Map<String, Object> arguments) {

        Object rawQuestion = arguments.get(
                LEGAL_QUESTION_ARGUMENT);

        if (rawQuestion == null) {
            throw new IllegalArgumentException(
                    "Missing required tool argument: "
                            + LEGAL_QUESTION_ARGUMENT);
        }

        String legalQuestion = rawQuestion.toString()
                .trim();

        if (legalQuestion.isEmpty()) {
            throw new IllegalArgumentException(
                    "Tool argument legalQuestion must not be blank");
        }

        return legalQuestion;
    }

    /**
     * 将 Retrieval Runtime Result 转换为
     * Tool 层可以返回的文本结果。
     *
     * <p>
     * 第一版保留：
     * </p>
     *
     * <ul>
     * <li>effective query</li>
     * <li>document count</li>
     * <li>document text</li>
     * <li>常用来源 metadata</li>
     * </ul>
     */
    private String formatRetrievalResult(
            RetrieverContext context) {

        if (!context.hasDocuments()) {
            return NO_KNOWLEDGE_FOUND;
        }

        StringBuilder builder = new StringBuilder();

        builder.append(
                "有效检索问题：")
                .append(
                        context.effectiveQuery())
                .append(
                        System.lineSeparator());

        builder.append(
                "检索文档数量：")
                .append(
                        context.documentCount())
                .append(
                        System.lineSeparator())
                .append(
                        System.lineSeparator());

        List<Document> documents = context.getDocuments();

        for (int index = 0; index < documents.size(); index++) {

            Document document = documents.get(index);

            appendDocument(
                    builder,
                    document,
                    index + 1);

            if (index < documents.size() - 1) {
                builder.append(
                        System.lineSeparator())
                        .append(
                                "---")
                        .append(
                                System.lineSeparator())
                        .append(
                                System.lineSeparator());
            }
        }

        return builder.toString()
                .trim();
    }

    /**
     * 格式化单个检索 Document。
     */
    private void appendDocument(
            StringBuilder builder,
            Document document,
            int index) {

        builder.append(
                "参考资料 ")
                .append(index)
                .append(
                        System.lineSeparator());

        appendMetadata(
                builder,
                document);

        builder.append(
                "内容：")
                .append(
                        System.lineSeparator())
                .append(
                        normalizeDocumentText(
                                document))
                .append(
                        System.lineSeparator());
    }

    /**
     * 保留当前 RAG 数据中已经存在的常用来源信息。
     */
    private void appendMetadata(
            StringBuilder builder,
            Document document) {

        Map<String, Object> metadata = document.getMetadata();

        appendMetadataIfPresent(
                builder,
                metadata,
                "file_name",
                "来源");

        appendMetadataIfPresent(
                builder,
                metadata,
                "page_number",
                "页码");

        appendMetadataIfPresent(
                builder,
                metadata,
                "chunk_index",
                "Chunk");

        if (document.getScore() != null) {
            builder.append(
                    "相关度：")
                    .append(
                            document.getScore())
                    .append(
                            System.lineSeparator());
        }
    }

    private void appendMetadataIfPresent(
            StringBuilder builder,
            Map<String, Object> metadata,
            String key,
            String label) {

        if (metadata == null
                || metadata.isEmpty()) {
            return;
        }

        Object value = metadata.get(key);

        if (value == null) {
            return;
        }

        String normalizedValue = value.toString()
                .trim();

        if (normalizedValue.isEmpty()) {
            return;
        }

        builder.append(label)
                .append("：")
                .append(normalizedValue)
                .append(
                        System.lineSeparator());
    }

    private String normalizeDocumentText(
            Document document) {

        String text = document.getText();

        if (text == null
                || text.isBlank()) {

            return "[无有效文本]";
        }

        return text.trim();
    }

    private String resolveErrorMessage(
            RuntimeException exception) {

        String message = exception.getMessage();

        if (message == null
                || message.isBlank()) {

            return exception.getClass()
                    .getSimpleName();
        }

        return message.trim();
    }
}