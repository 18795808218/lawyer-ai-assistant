package com.quince.lawyeraiassistant.prompt.knowledge;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultKnowledgeFormatterTest {

    private KnowledgeFormatter knowledgeFormatter;

    @BeforeEach
    void setUp() {
        knowledgeFormatter = new DefaultKnowledgeFormatter();
    }

    @Test
    void shouldFormatSingleDocument() {
        Document document = Document.builder()
                .text(
                        "第八十七条 用人单位违法解除或者终止"
                                + "劳动合同的，应当支付赔偿金。")
                .metadata(
                        Map.of(
                                "file_name",
                                "劳动合同法.pdf",
                                "page_number",
                                24,
                                "chunk_index",
                                0))
                .score(0.7013132123)
                .build();

        String result = knowledgeFormatter.format(
                List.of(document));

        assertTrue(
                result.contains("## 参考资料 1"));

        assertTrue(
                result.contains(
                        "- 来源：劳动合同法.pdf"));

        assertTrue(
                result.contains("- 页码：24"));

        assertTrue(
                result.contains("- Chunk：0"));

        assertTrue(
                result.contains("- 相关度：0.7013"));

        assertTrue(
                result.contains(
                        "第八十七条 用人单位违法解除"));
    }

    @Test
    void shouldFormatMultipleDocumentsInOriginalOrder() {
        Document firstDocument = Document.builder()
                .text("第四十六条 经济补偿适用情形。")
                .metadata(
                        Map.of(
                                "file_name",
                                "劳动合同法.pdf",
                                "page_number",
                                13))
                .score(0.91)
                .build();

        Document secondDocument = Document.builder()
                .text("第四十七条 经济补偿计算标准。")
                .metadata(
                        Map.of(
                                "file_name",
                                "劳动合同法.pdf",
                                "page_number",
                                14))
                .score(0.88)
                .build();

        String result = knowledgeFormatter.format(
                List.of(
                        firstDocument,
                        secondDocument));

        assertTrue(
                result.contains("## 参考资料 1"));

        assertTrue(
                result.contains("## 参考资料 2"));

        int firstPosition = result.indexOf("第四十六条");

        int secondPosition = result.indexOf("第四十七条");

        assertTrue(firstPosition >= 0);
        assertTrue(secondPosition > firstPosition);

        assertTrue(result.contains("---"));
    }

    @Test
    void shouldPreferLawNameOverFileName() {
        Document document = Document.builder()
                .text("第四十七条 经济补偿计算标准。")
                .metadata(
                        Map.of(
                                "lawName",
                                "中华人民共和国劳动合同法",
                                "file_name",
                                "labor-law.pdf"))
                .build();

        String result = knowledgeFormatter.format(
                List.of(document));

        assertTrue(
                result.contains(
                        "- 来源：中华人民共和国劳动合同法"));

        assertFalse(
                result.contains(
                        "- 来源：labor-law.pdf"));
    }

    @Test
    void shouldUseUnknownSourceWhenMetadataIsMissing() {
        Document document = new Document(
                "没有来源 Metadata 的法律内容。");

        String result = knowledgeFormatter.format(
                List.of(document));

        assertTrue(
                result.contains("- 来源：未知来源"));
    }

    @Test
    void shouldOmitOptionalMetadataWhenNotAvailable() {
        Document document = new Document(
                "只包含正文的法律知识。");

        String result = knowledgeFormatter.format(
                List.of(document));

        assertTrue(
                result.contains("- 来源：未知来源"));

        assertFalse(
                result.contains("- 页码："));

        assertFalse(
                result.contains("- Chunk："));

        assertFalse(
                result.contains("- 相关度："));
    }

    @Test
    void shouldSupportCamelCaseMetadataKeys() {
        Document document = Document.builder()
                .text("使用驼峰 Metadata 的知识内容。")
                .metadata(
                        Map.of(
                                "fileName",
                                "contract.pdf",
                                "chunkIndex",
                                "3",
                                "page",
                                "5"))
                .build();

        String result = knowledgeFormatter.format(
                List.of(document));

        assertTrue(
                result.contains("- 来源：contract.pdf"));

        assertTrue(
                result.contains("- 页码：5"));

        assertTrue(
                result.contains("- Chunk：3"));
    }

    @Test
    void shouldIgnoreInvalidNumericMetadata() {
        Document document = Document.builder()
                .text("包含非法页码的知识内容。")
                .metadata(
                        Map.of(
                                "file_name",
                                "law.pdf",
                                "page_number",
                                "not-a-number",
                                "chunk_index",
                                "invalid"))
                .build();

        String result = knowledgeFormatter.format(
                List.of(document));

        assertTrue(
                result.contains("- 来源：law.pdf"));

        assertFalse(
                result.contains("- 页码："));

        assertFalse(
                result.contains("- Chunk："));
    }

    @Test
    void shouldIgnoreNullAndBlankDocuments() {
        Document validDocument = new Document(
                "有效法律知识。");

        Document blankDocument = new Document("   ");

        String result = knowledgeFormatter.format(
                java.util.Arrays.asList(
                        null,
                        blankDocument,
                        validDocument));

        assertTrue(
                result.contains("有效法律知识。"));

        assertEquals(
                1,
                countOccurrences(
                        result,
                        "## 参考资料"));
    }

    @Test
    void shouldReturnEmptyStringWhenDocumentsAreNull() {
        String result = knowledgeFormatter.format(null);

        assertEquals("", result);
    }

    @Test
    void shouldReturnEmptyStringWhenDocumentsAreEmpty() {
        String result = knowledgeFormatter.format(
                List.of());

        assertEquals("", result);
    }

    @Test
    void shouldReturnEmptyStringWhenAllDocumentsAreInvalid() {
        String result = knowledgeFormatter.format(
                List.of(
                        new Document("   ")));

        assertEquals("", result);
    }

    @Test
    void shouldTrimDocumentContent() {
        Document document = new Document(
                "   劳动合同法正文内容。   ");

        String result = knowledgeFormatter.format(
                List.of(document));

        assertTrue(
                result.contains("劳动合同法正文内容。"));

        assertFalse(
                result.contains(
                        "   劳动合同法正文内容。   "));
    }

    private int countOccurrences(
            String source,
            String target) {
        int count = 0;
        int index = 0;

        while ((index = source.indexOf(
                target,
                index)) >= 0) {
            count++;
            index += target.length();
        }

        return count;
    }
}