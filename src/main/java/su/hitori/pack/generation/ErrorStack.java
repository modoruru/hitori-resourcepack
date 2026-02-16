package su.hitori.pack.generation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ErrorStack {

    private final Map<String, List<String>> errors = new HashMap<>();

    public void add(String text, String entry) {
        errors.computeIfAbsent(text, _ -> new ArrayList<>()).add(entry);
    }

    public List<Error> getErrors() {
        List<Error> errors = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : this.errors.entrySet()) {
            errors.add(new Error(entry.getKey(), entry.getValue()));
        }
        return errors;
    }

    public record Error(String text, List<String> entries) {

    }

}
