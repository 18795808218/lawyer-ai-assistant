package com.quince.lawyeraiassistant.rag.splitter;

import org.springframework.ai.transformer.splitter.TextSplitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 面向中文法律文本的简化切分器。
 *
 * 切分规则：
 * 1. 按字符数量控制 Chunk 大小；
 * 2. 优先在中文标点或换行处切分；
 * 3. 支持相邻 Chunk 内容重叠；
 * 4. 过滤过短 Chunk。
 *
 * 注意：
 * 本类是字符切分器，不是真正的 Token 切分器。
 */
public class LegalTextSplitter extends TextSplitter {

    /**
     * 优先使用的中文语义边界。
     */
    private static final Set<Character> BOUNDARY_CHARACTERS = Set.of(
            '。',
            '！',
            '？',
            '；',
            '：',
            '\n');

    /**
     * 每个 Chunk 的目标字符数。
     */
    private final int chunkSize;

    /**
     * 相邻 Chunk 重叠字符数。
     */
    private final int chunkOverlap;

    /**
     * 在目标位置之前寻找标点的范围。
     */
    private final int boundarySearchSize;

    /**
     * 最小有效 Chunk 字符数。
     */
    private final int minChunkSize;

    public LegalTextSplitter(
            int chunkSize,
            int chunkOverlap,
            int boundarySearchSize,
            int minChunkSize) {

        validateArguments(
                chunkSize,
                chunkOverlap,
                boundarySearchSize,
                minChunkSize);

        this.chunkSize = chunkSize;
        this.chunkOverlap = chunkOverlap;
        this.boundarySearchSize = boundarySearchSize;
        this.minChunkSize = minChunkSize;
    }

    /**
     * TextSplitter 的模板方法。
     *
     * 父类负责：
     * - 遍历 Document
     * - 调用本方法切分字符串
     * - 将字符串重新包装成 Document
     * - 处理原始 Metadata
     *
     * 当前类只负责决定文本如何切分。
     */
    @Override
    protected List<String> splitText(String text) {

        if (text == null || text.isBlank()) {
            return List.of();
        }

        String normalizedText = normalizeText(text);

        if (normalizedText.length() <= chunkSize) {
            return createSingleChunk(normalizedText);
        }

        return splitLargeText(normalizedText);
    }

    private List<String> splitLargeText(String text) {

        List<String> chunks = new ArrayList<>();

        int start = 0;

        while (start < text.length()) {

            int preferredEnd = Math.min(
                    start + chunkSize,
                    text.length());

            int actualEnd = findActualEnd(
                    text,
                    start,
                    preferredEnd);

            /*
             * 防御性处理：
             * 如果没有找到有效结束位置，强制推进。
             */
            if (actualEnd <= start) {
                actualEnd = preferredEnd;
            }

            String chunk = text
                    .substring(start, actualEnd)
                    .trim();

            addChunkIfValid(chunks, chunk);

            if (actualEnd >= text.length()) {
                break;
            }

            int nextStart = actualEnd - chunkOverlap;

            /*
             * 防止 overlap 或异常边界导致游标不前进。
             */
            if (nextStart <= start) {
                nextStart = actualEnd;
            }

            start = skipLeadingWhitespace(text, nextStart);
        }

        return chunks;
    }

    /**
     * 在理想结束位置之前寻找最近的中文标点。
     */
    private int findActualEnd(
            String text,
            int start,
            int preferredEnd) {

        if (preferredEnd >= text.length()) {
            return text.length();
        }

        int searchStart = Math.max(
                start + minChunkSize,
                preferredEnd - boundarySearchSize);

        for (int index = preferredEnd - 1; index >= searchStart; index--) {

            char currentCharacter = text.charAt(index);

            if (BOUNDARY_CHARACTERS.contains(currentCharacter)) {
                /*
                 * +1 的目的是把标点保留在当前 Chunk 中。
                 */
                return index + 1;
            }
        }

        /*
         * 没有找到合适标点时，按照目标长度硬切。
         */
        return preferredEnd;
    }

    private List<String> createSingleChunk(String text) {

        String chunk = text.trim();

        if (chunk.length() < minChunkSize) {
            return List.of();
        }

        return List.of(chunk);
    }

    private void addChunkIfValid(
            List<String> chunks,
            String chunk) {

        if (chunk.isBlank()) {
            return;
        }

        if (chunk.length() < minChunkSize) {
            return;
        }

        chunks.add(chunk);
    }

    /**
     * 规范化 PDF 中比较常见的换行和空白。
     *
     * 保留单个换行，因为换行也可以作为语义边界。
     */
    private String normalizeText(String text) {

        return text
                .replace("\r\n", "\n")
                .replace('\r', '\n')
                .replaceAll("[\\t\\x0B\\f ]+", " ")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
    }

    private int skipLeadingWhitespace(
            String text,
            int start) {

        int result = start;

        while (result < text.length()
                && Character.isWhitespace(text.charAt(result))) {

            result++;
        }

        return result;
    }

    private void validateArguments(
            int chunkSize,
            int chunkOverlap,
            int boundarySearchSize,
            int minChunkSize) {

        if (chunkSize <= 0) {
            throw new IllegalArgumentException(
                    "chunkSize 必须大于 0");
        }

        if (chunkOverlap < 0) {
            throw new IllegalArgumentException(
                    "chunkOverlap 不能小于 0");
        }

        if (chunkOverlap >= chunkSize) {
            throw new IllegalArgumentException(
                    "chunkOverlap 必须小于 chunkSize");
        }

        if (boundarySearchSize < 0) {
            throw new IllegalArgumentException(
                    "boundarySearchSize 不能小于 0");
        }

        if (boundarySearchSize >= chunkSize) {
            throw new IllegalArgumentException(
                    "boundarySearchSize 必须小于 chunkSize");
        }

        if (minChunkSize <= 0) {
            throw new IllegalArgumentException(
                    "minChunkSize 必须大于 0");
        }

        if (minChunkSize > chunkSize) {
            throw new IllegalArgumentException(
                    "minChunkSize 不能大于 chunkSize");
        }
    }
}