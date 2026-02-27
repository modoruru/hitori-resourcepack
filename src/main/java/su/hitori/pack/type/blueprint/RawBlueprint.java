package su.hitori.pack.type.blueprint;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.json.JSONObject;
import su.hitori.api.util.FileUtil;
import su.hitori.api.util.JSONUtil;

import java.io.File;
import java.util.Optional;

public record RawBlueprint(Key key, JSONObject body) implements Keyed {

    public static RawBlueprint create(File file, boolean ignoreExtension) {
        if(!file.exists()) return null;

        var nameAndExtension = FileUtil.getNameAndExtension(file);
        if(!ignoreExtension && !nameAndExtension.second().equalsIgnoreCase("json"))
            return null;

        JSONObject body;
        try {
            body = JSONUtil.readFile(file);
        }
        catch (Exception _) {
            return null;
        }

        String exportNamespace = Optional.ofNullable(body.optJSONObject("settings"))
                .map(json -> json.optString("export_namespace", null))
                .orElse(null);
        if(exportNamespace == null) return null;

        return new RawBlueprint(
                Key.key(exportNamespace, nameAndExtension.first().toLowerCase()),
                body
        );
    }

}
