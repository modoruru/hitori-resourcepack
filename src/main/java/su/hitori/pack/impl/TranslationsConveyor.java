package su.hitori.pack.impl;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import su.hitori.api.util.FileUtil;
import su.hitori.pack.generation.GenerationContext;
import su.hitori.pack.type.Translations;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public final class TranslationsConveyor extends AbstractConveyor<Translations> {

    public TranslationsConveyor(Key key) {
        super(key);
    }

    @Override
    public void generate(GenerationContext context) {
        if(collectingOrGenerating) return;
        collectingOrGenerating = true;

        Map<Translations.Locale, JSONObject> languages = combineLanguages();

        File langFolder = new File(
                context.folder(),
                "assets/minecraft/lang/"
        );
        langFolder.mkdirs();

        for (Map.Entry<Translations.Locale, JSONObject> entry : languages.entrySet()) {
            FileUtil.writeTextToFile(
                    new File(
                            langFolder,
                            entry.getKey().name() + ".json"
                    ),
                    entry.getValue().toString()
            );
        }

        collectingOrGenerating = false;
    }

    private @NotNull Map<Translations.Locale, JSONObject> combineLanguages() {
        Map<Translations.Locale, JSONObject> languages = new HashMap<>();

        for (Translations value : snapshots.values()) {
            for (Map.Entry<Translations.Locale, Map<String, String>> entry : value.locales().entrySet()) {
                Map<String, String> keys = entry.getValue();
                if(!keys.isEmpty()) {
                    JSONObject object = languages.computeIfAbsent(entry.getKey(), (_) -> new JSONObject());
                    for (Map.Entry<String, String> translationEntry : keys.entrySet()) {
                        object.put(translationEntry.getKey(), translationEntry.getValue());
                    }
                }
            }
        }

        return languages;
    }

}
