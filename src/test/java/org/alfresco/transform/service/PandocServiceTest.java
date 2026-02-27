package org.alfresco.transform.service;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PandocServiceTest {

    @Test
    void buildCommandUsesConfiguredTocDefaultsWhenOptionsAreUnset() {
        PandocService service = new PandocService();
        ReflectionTestUtils.setField(service, "defaultTocEnabled", true);
        ReflectionTestUtils.setField(service, "defaultTocDepth", 4);
        ReflectionTestUtils.setField(service, "pdfEngine", "xelatex");

        List<String> command = service.buildCommand(
                new File("/tmp/input.md"),
                Path.of("/tmp/output.pdf"),
                "application/pdf",
                null,
                null);

        assertThat(command).contains("--toc", "--toc-depth=4", "--pdf-engine=xelatex");
    }

    @Test
    void buildCommandLetsExplicitOptionsOverrideConfiguredDefaults() {
        PandocService service = new PandocService();
        ReflectionTestUtils.setField(service, "defaultTocEnabled", true);
        ReflectionTestUtils.setField(service, "defaultTocDepth", 4);
        ReflectionTestUtils.setField(service, "pdfEngine", "xelatex");

        List<String> command = service.buildCommand(
                new File("/tmp/input.md"),
                Path.of("/tmp/output.pdf"),
                "application/pdf",
                false,
                2);

        assertThat(command).doesNotContain("--toc", "--toc-depth=2", "--toc-depth=4");
    }
}
