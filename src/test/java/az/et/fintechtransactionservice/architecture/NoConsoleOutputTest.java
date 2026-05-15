package az.et.fintechtransactionservice.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NoConsoleOutputTest {

    @Test
    @DisplayName("Main source code should not use console output")
    void shouldNotUseConsoleOutputInMainCode() throws IOException {
        Path mainSource = Path.of("src", "main", "java");
        List<Path> offenders;
        try (var paths = Files.walk(mainSource)) {
            offenders = paths
                    .filter(path -> path.toString().endsWith(".java"))
                    .filter(NoConsoleOutputTest::containsConsoleOutput)
                    .toList();
        }

        assertThat(offenders).isEmpty();
    }

    private static boolean containsConsoleOutput(Path path) {
        try {
            String content = Files.readString(path);
            return content.contains("System.out") || content.contains("System.err");
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read " + path, exception);
        }
    }
}

