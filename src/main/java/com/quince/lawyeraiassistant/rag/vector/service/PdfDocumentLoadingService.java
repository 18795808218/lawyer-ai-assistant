package com.quince.lawyeraiassistant.rag.vector.service;

import com.quince.lawyeraiassistant.rag.splitter.LegalTextSplitter;
import com.quince.lawyeraiassistant.retrieval.parent.provider.ParentDocumentStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PdfDocumentLoadingService
        implements DocumentLoadingService {

    private final LegalTextSplitter legalTextSplitter;

    private final ParentDocumentStore parentDocumentStore;

    @Override
    public List<Document> loadAndSplit(
            String location) {
        Resource[] resources = resolvePdfResources(location);

        List<Document> sourceDocuments = Arrays.stream(resources)
                .flatMap(resource -> new PagePdfDocumentReader(
                        resource)
                        .get()
                        .stream())
                .filter(document -> document != null
                        && document.getText() != null
                        && !document.getText().isBlank())
                .toList();

        if (sourceDocuments.isEmpty()) {
            return List.of();
        }

        List<Document> chunks = registerParentsAndSplit(
                sourceDocuments);

        log.info(
                "PDF documents loaded. resources={}, parents={}, chunks={}",
                resources.length,
                sourceDocuments.size(),
                chunks.size());

        return chunks;
    }

    List<Document> registerParentsAndSplit(
            List<Document> sourceDocuments) {
        if (sourceDocuments == null
                || sourceDocuments.isEmpty()) {
            return List.of();
        }

        parentDocumentStore.saveAll(
                sourceDocuments);

        return legalTextSplitter.split(
                sourceDocuments);
    }

    private Resource[] resolvePdfResources(
            String location) {
        String pattern = normalizeLocation(location);

        try {
            return new PathMatchingResourcePatternResolver()
                    .getResources(pattern);
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Failed to load knowledge-base resources from: "
                            + pattern,
                    exception);
        }
    }

    private String normalizeLocation(
            String location) {
        if (location.endsWith("/")) {
            return location + "*.pdf";
        }

        return location + "/*.pdf";
    }
}