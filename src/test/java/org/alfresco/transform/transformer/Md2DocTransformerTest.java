package org.alfresco.transform.transformer;

import org.alfresco.transform.service.PandocService;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class Md2DocTransformerTest {

    @Test
    void transformTreatsBlankTocOptionsAsUnset() throws Exception {
        Path outputFile = Files.createTempFile("md2doc-transformer-test", ".pdf");
        Files.writeString(outputFile, "converted", StandardCharsets.UTF_8);

        CapturingPandocService pandocService = new CapturingPandocService(outputFile.toFile());
        Md2DocTransformer transformer = new Md2DocTransformer(pandocService);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        transformer.transform(
                "text/markdown",
                new ByteArrayInputStream("# Title".getBytes(StandardCharsets.UTF_8)),
                "application/pdf",
                outputStream,
                Map.of("tocEnabled", "", "tocDepth", ""),
                null);

        assertThat(outputStream.toString(StandardCharsets.UTF_8)).isEqualTo("converted");
        assertThat(pandocService.tocEnabled).isNull();
        assertThat(pandocService.tocDepth).isNull();
        assertThat(pandocService.targetMimetype).isEqualTo("application/pdf");

        Files.deleteIfExists(outputFile);
    }

    private static final class CapturingPandocService extends PandocService {
        private final File outputFile;
        private Boolean tocEnabled;
        private Integer tocDepth;
        private String targetMimetype;

        private CapturingPandocService(File outputFile) {
            this.outputFile = outputFile;
        }

        @Override
        public File convert(File inputFile, String targetMimetype, Boolean tocEnabled, Integer tocDepth) {
            this.targetMimetype = targetMimetype;
            this.tocEnabled = tocEnabled;
            this.tocDepth = tocDepth;
            return outputFile;
        }
    }
}
