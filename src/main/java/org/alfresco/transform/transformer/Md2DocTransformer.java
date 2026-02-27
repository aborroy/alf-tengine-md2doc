package org.alfresco.transform.transformer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alfresco.transform.service.PandocService;
import org.alfresco.transform.base.CustomTransformer;
import org.alfresco.transform.base.TransformManager;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class Md2DocTransformer implements CustomTransformer {

    private final PandocService pandocService;

    @Override
    public String getTransformerName() {
        return "md2doc";
    }

    @Override
    public void transform(String sourceMimetype,
                          InputStream inputStream,
                          String targetMimetype,
                          OutputStream outputStream,
                          Map<String, String> transformOptions,
                          TransformManager transformManager) throws Exception {

        // Write input stream to a temp file
        Path workDir = Files.createTempDirectory("md2doc");
        File inputFile = workDir.resolve("input.md").toFile();

        try (OutputStream tempOut = new FileOutputStream(inputFile)) {
            inputStream.transferTo(tempOut);
        }

        // Parse transform options
        Boolean tocEnabled = parseBooleanOption(transformOptions.get("tocEnabled"));
        Integer tocDepth = parseIntegerOption("tocDepth", transformOptions.get("tocDepth"));

        try {
            log.info("Converting {} → {} (toc={}, tocDepth={})",
                    sourceMimetype, targetMimetype, tocEnabled, tocDepth);

            File outputFile = pandocService.convert(inputFile, targetMimetype, tocEnabled, tocDepth);

            // Stream result back
            try (InputStream resultStream = new FileInputStream(outputFile)) {
                resultStream.transferTo(outputStream);
            }
        } finally {
            // Clean up temp files
            deleteRecursively(workDir.toFile());
        }
    }

    private void deleteRecursively(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursively(child);
                }
            }
        }
        if (!file.delete()) {
            log.warn("Failed to delete temp file: {}", file);
        }
    }

    private Boolean parseBooleanOption(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Boolean.parseBoolean(value);
    }

    private Integer parseIntegerOption(String optionName, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid integer value for " + optionName + ": " + value, ex);
        }
    }
}
