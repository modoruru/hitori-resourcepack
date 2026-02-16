package su.hitori.pack.generation;

import java.io.File;

public record GenerationContext(File folder, ErrorStack errorStack) {
}
