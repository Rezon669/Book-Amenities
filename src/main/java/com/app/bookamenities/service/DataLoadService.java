package com.app.bookamenities.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class DataLoadService {

    private final VectorStore vectorStore;
    private final JdbcTemplate jdbcTemplate;

    public DataLoadService(VectorStore vectorStore, JdbcTemplate jdbcTemplate) {
        this.vectorStore = vectorStore;
        this.jdbcTemplate = jdbcTemplate;
    }

    //@PostConstruct
    public void loadData() {

        try {

            log.info("Deleting old RAG data...");

            // Delete existing vectors
            jdbcTemplate.execute("TRUNCATE TABLE vector_store");

            log.info("Old RAG data deleted successfully");

            ClassPathResource resource =
                    new ClassPathResource("docs/book_amenities_user_guide.pdf");

            log.info("Processing file: {}", resource.getFilename());

            List<Document> docs = getDocsFromPdf(resource);

            log.info("Extracted {} documents from file: {}",
                    docs.size(),
                    resource.getFilename());

            List<Document> chunks = splitDocuments(docs);

            log.info("Split documents into {} chunks for file: {}",
                    chunks.size(),
                    resource.getFilename());

            vectorStore.add(chunks);

            log.info("Added {} chunks to vector store for file: {}",
                    chunks.size(),
                    resource.getFilename());

        } catch (Exception e) {

            log.error("Error processing PDF files", e);

            throw new RuntimeException("Error processing PDF files", e);
        }
    }

    public List<Document> getDocsFromPdf(ClassPathResource resource) {

        PagePdfDocumentReader pdfReader =
                new PagePdfDocumentReader(
                        resource,
                        PdfDocumentReaderConfig.builder()
                                .withPageTopMargin(0)
                                .withPageExtractedTextFormatter(
                                        ExtractedTextFormatter.builder()
                                                .withNumberOfTopTextLinesToDelete(0)
                                                .build()
                                )
                                .withPagesPerDocument(1)
                                .build()
                );

        return pdfReader.read();
    }

    public List<Document> splitDocuments(List<Document> documents) {

        TokenTextSplitter splitter = TokenTextSplitter.builder()
                .withChunkSize(1000)
                .withMinChunkSizeChars(400)
                .withMinChunkLengthToEmbed(10)
                .withMaxNumChunks(5000)
                .withKeepSeparator(true)
                .build();

        return splitter.apply(documents);
    }
}