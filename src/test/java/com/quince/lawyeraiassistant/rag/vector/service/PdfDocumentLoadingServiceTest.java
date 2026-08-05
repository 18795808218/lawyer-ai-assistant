package com.quince.lawyeraiassistant.rag.vector.service;

import com.quince.lawyeraiassistant.rag.splitter.LegalTextSplitter;
import com.quince.lawyeraiassistant.retrieval.parent.provider.ParentDocumentStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PdfDocumentLoadingServiceTest {

    private LegalTextSplitter legalTextSplitter;

    private ParentDocumentStore parentDocumentStore;

    private PdfDocumentLoadingService service;

    @BeforeEach
    void setUp() {
        legalTextSplitter = mock(LegalTextSplitter.class);

        parentDocumentStore = mock(ParentDocumentStore.class);

        service = new PdfDocumentLoadingService(
                legalTextSplitter,
                parentDocumentStore);
    }

    @Test
    void shouldSaveParentsBeforeSplitting() {
        List<Document> parents = List.of(
                createDocument(
                        "parent-1",
                        "第一页完整内容"));

        List<Document> chunks = List.of(
                createDocument(
                        "chunk-1",
                        "第一页切块内容"));

        when(
                legalTextSplitter.split(parents)).thenReturn(chunks);

        List<Document> result = service.registerParentsAndSplit(
                parents);

        var order = inOrder(
                parentDocumentStore,
                legalTextSplitter);

        order.verify(
                parentDocumentStore).saveAll(parents);

        order.verify(
                legalTextSplitter).split(parents);

        assertSame(chunks, result);
    }

    @Test
    void shouldReturnEmptyWithoutSavingOrSplittingWhenParentsAreEmpty() {
        List<Document> result = service.registerParentsAndSplit(
                List.of());

        assertEquals(
                List.of(),
                result);

        verify(
                parentDocumentStore,
                org.mockito.Mockito.never()).saveAll(
                        org.mockito.ArgumentMatchers.any());

        verify(
                legalTextSplitter,
                never()).split(
                        org.mockito.ArgumentMatchers
                                .<Document>anyList());
    }

    private Document createDocument(
            String id,
            String text) {
        return new Document(
                id,
                text,
                Map.of());
    }
}