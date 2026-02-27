package org.alfresco.transform.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class PandocService {

    private static final String MIMETYPE_DOCX =
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    private static final String MIMETYPE_PDF = "application/pdf";

    @Value("${transform.pandoc.toc.enabled}")
    private boolean defaultTocEnabled;

    @Value("${transform.pandoc.toc.depth}")
    private int defaultTocDepth;

    @Value("${transform.pandoc.pdf.engine}")
    private String pdfEngine;

    public File convert(File inputFile, String targetMimetype,
                        Boolean tocEnabled, Integer tocDepth) throws IOException, InterruptedException {

        String outputExtension = resolveExtension(targetMimetype);

        Path workDir = inputFile.getParentFile().toPath();
        Path outputPath = workDir.resolve("output" + outputExtension);

        List<String> command = buildCommand(inputFile, outputPath, targetMimetype, tocEnabled, tocDepth);

        log.info("Running Pandoc: {}", String.join(" ", command));

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(workDir.toFile());
        pb.redirectErrorStream(true);
        Process process = pb.start();

        try (BufferedReader reader = process.inputReader()) {
            reader.lines().forEach(log::info);
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IllegalStateException("Pandoc exited with code " + exitCode);
        }

        if (!Files.exists(outputPath)) {
            throw new IllegalStateException("Pandoc did not produce output file: " + outputPath);
        }

        log.info("Pandoc conversion successful: {} ({} bytes)", outputPath, Files.size(outputPath));
        return outputPath.toFile();
    }

    List<String> buildCommand(File inputFile, Path outputPath,
                              String targetMimetype,
                              Boolean tocEnabled, Integer tocDepth) {
        List<String> command = new ArrayList<>();
        command.add("pandoc");
        command.add(inputFile.getAbsolutePath());
        command.add("-o");
        command.add(outputPath.toString());
        command.add("--standalone");

        boolean resolvedTocEnabled = tocEnabled != null ? tocEnabled : defaultTocEnabled;
        int resolvedTocDepth = tocDepth != null ? tocDepth : defaultTocDepth;

        if (resolvedTocEnabled) {
            command.add("--toc");
            command.add("--toc-depth=" + resolvedTocDepth);
        }

        if (MIMETYPE_PDF.equals(targetMimetype)) {
            command.add("--pdf-engine=" + pdfEngine);
            // Better default margins for PDF
            command.add("-V");
            command.add("geometry:margin=2.5cm");
        }

        // Reference doc for DOCX styling (if present in classpath)
        if (MIMETYPE_DOCX.equals(targetMimetype)) {
            File referenceDoc = new File("/app/reference.docx");
            if (referenceDoc.exists()) {
                command.add("--reference-doc=" + referenceDoc.getAbsolutePath());
                log.info("Using reference DOCX template for styling");
            }
        }

        return command;
    }

    private String resolveExtension(String targetMimetype) {
        return switch (targetMimetype) {
            case MIMETYPE_DOCX -> ".docx";
            case MIMETYPE_PDF -> ".pdf";
            default -> throw new IllegalArgumentException("Unsupported target mimetype: " + targetMimetype);
        };
    }
}
