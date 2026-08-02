package com.quince.lawyeraiassistant.prompt.knowledge;

import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * KnowledgeFormatter 的默认实现。
 *
 * <p>
 * 将 Spring AI Document 转换为结构化 Markdown 文本，
 * 供 Prompt 模板中的 {knowledge} 变量使用。
 * </p>
 */
@Component
public class DefaultKnowledgeFormatter
        implements KnowledgeFormatter {

    private static final String UNKNOWN_SOURCE = "未知来源";

    private static final String FILE_NAME = "file_name";

    private static final String FILE_NAME_CAMEL = "fileName";

    private static final String FILENAME = "filename";

    private static final String SOURCE = "source";

    private static final String LAW_NAME = "law_name";

    private static final String LAW_NAME_CAMEL = "lawName";

    private static final String PAGE_NUMBER = "page_number";

    private static final String PAGE = "page";

    private static final String CHUNK_INDEX = "chunk_index";

    private static final String CHUNK_INDEX_CAMEL = "chunkIndex";

    /**
     * 将文档列表格式化为 Markdown 知识上下文。
     */
    @Override
    public String format(List<Document> documents) {
        List<KnowledgeBlock> blocks = toKnowledgeBlocks(documents);

        if (blocks.isEmpty()) {
            return "";
        }

        return renderBlocks(blocks);
    }

    /**
     * 将 Document 转换为 KnowledgeBlock。
     */
    private List<KnowledgeBlock> toKnowledgeBlocks(
            List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            return List.of();
        }

        List<KnowledgeBlock> blocks = new ArrayList<>();

        int blockIndex = 1;

        for (Document document : documents) {
            if (!isValidDocument(document)) {
                continue;
            }

            blocks.add(
                    toKnowledgeBlock(
                            document,
                            blockIndex));

            blockIndex++;
        }

        return List.copyOf(blocks);
    }

    /**
     * 判断 Document 是否包含可使用的文本。
     */
    private boolean isValidDocument(Document document) {
        return document != null
                && document.getText() != null
                && !document.getText().isBlank();
    }

    /**
     * 转换单个 Document。
     */
    private KnowledgeBlock toKnowledgeBlock(
            Document document,
            int index) {
        Map<String, Object> metadata = document.getMetadata();

        return KnowledgeBlock.builder()
                .index(index)
                .source(resolveSource(metadata))
                .pageNumber(
                        resolveInteger(
                                metadata,
                                PAGE_NUMBER,
                                PAGE))
                .chunkIndex(
                        resolveInteger(
                                metadata,
                                CHUNK_INDEX,
                                CHUNK_INDEX_CAMEL))
                .score(document.getScore())
                .content(document.getText().trim())
                .build();
    }

    /**
     * 将 KnowledgeBlock 列表渲染为 Markdown。
     */
    private String renderBlocks(
            List<KnowledgeBlock> blocks) {
        StringBuilder builder = new StringBuilder();

        for (int index = 0; index < blocks.size(); index++) {

            KnowledgeBlock block = blocks.get(index);

            appendBlock(builder, block);

            if (index < blocks.size() - 1) {
                builder.append(System.lineSeparator())
                        .append("---")
                        .append(System.lineSeparator())
                        .append(System.lineSeparator());
            }
        }

        return builder.toString().trim();
    }

    /**
     * 渲染单个知识块。
     */
    private void appendBlock(
            StringBuilder builder,
            KnowledgeBlock block) {
        builder.append("## 参考资料 ")
                .append(block.getIndex())
                .append(System.lineSeparator())
                .append(System.lineSeparator());

        builder.append("- 来源：")
                .append(block.getSource())
                .append(System.lineSeparator());

        if (block.getPageNumber() != null) {
            builder.append("- 页码：")
                    .append(block.getPageNumber())
                    .append(System.lineSeparator());
        }

        if (block.getChunkIndex() != null) {
            builder.append("- Chunk：")
                    .append(block.getChunkIndex())
                    .append(System.lineSeparator());
        }

        if (block.getScore() != null) {
            builder.append("- 相关度：")
                    .append(formatScore(block.getScore()))
                    .append(System.lineSeparator());
        }

        builder.append(System.lineSeparator())
                .append("### 内容")
                .append(System.lineSeparator())
                .append(System.lineSeparator())
                .append(block.getContent())
                .append(System.lineSeparator());
    }

    /**
     * 解析知识来源。
     */
    private String resolveSource(
            Map<String, Object> metadata) {
        String source = firstNonBlankString(
                metadata,
                LAW_NAME,
                LAW_NAME_CAMEL,
                FILE_NAME,
                FILE_NAME_CAMEL,
                FILENAME,
                SOURCE);

        return source == null
                ? UNKNOWN_SOURCE
                : source;
    }

    /**
     * 获取第一个非空字符串类型的 Metadata。
     */
    private String firstNonBlankString(
            Map<String, Object> metadata,
            String... keys) {
        if (metadata == null || metadata.isEmpty()) {
            return null;
        }

        for (String key : keys) {
            Object value = metadata.get(key);

            if (value == null) {
                continue;
            }

            String text = value.toString().trim();

            if (!text.isEmpty()) {
                return text;
            }
        }

        return null;
    }

    /**
     * 从多个候选键中解析整数。
     */
    private Integer resolveInteger(
            Map<String, Object> metadata,
            String... keys) {
        if (metadata == null || metadata.isEmpty()) {
            return null;
        }

        for (String key : keys) {
            Object value = metadata.get(key);

            Integer result = convertToInteger(value);

            if (result != null) {
                return result;
            }
        }

        return null;
    }

    /**
     * 将 Metadata 值转换为 Integer。
     */
    private Integer convertToInteger(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Number number) {
            return number.intValue();
        }

        try {
            return Integer.valueOf(
                    value.toString().trim());
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    /**
     * 统一格式化相关度分数。
     */
    private String formatScore(Double score) {
        return String.format(
                Locale.ROOT,
                "%.4f",
                score);
    }
}