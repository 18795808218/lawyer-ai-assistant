package com.quince.lawyeraiassistant.rag.vector.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import com.quince.lawyeraiassistant.rag.splitter.LegalTextSplitter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PdfDocumentLoadingService implements DocumentLoadingService {

    private final LegalTextSplitter legalTextSplitter;

    @Override
    public List<Document> loadAndSplit(String location) {
        Resource[] resources = resolvePdfResources(location);

        List<Document> sourceDocuments = Arrays.stream(resources)
                .flatMap(resource -> new PagePdfDocumentReader(resource)
                        .get()
                        .stream())
                .toList();

        if (sourceDocuments.isEmpty()) {
            return List.of();
        }

        return legalTextSplitter.split(sourceDocuments);
    }

    private Resource[] resolvePdfResources(String location) {
        String pattern = normalizeLocation(location);

        try {
            return new PathMatchingResourcePatternResolver()
                    .getResources(pattern);
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Failed to load knowledge-base resources from: " + pattern,
                    exception);
        }
    }

    private String normalizeLocation(String location) {
        if (location.endsWith("/")) {
            return location + "*.pdf";
        }

        return location + "/*.pdf";
    }
}