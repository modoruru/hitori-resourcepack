package su.hitori.pack.type;

import org.json.JSONObject;
import org.jspecify.annotations.Nullable;
import su.hitori.api.util.IOUtil;
import su.hitori.api.util.JSONUtil;

import java.io.File;
import java.io.FileInputStream;
import java.util.Optional;

public record Texture(byte[] data, Optional<JSONObject> meta) {

    public static @Nullable Texture readFile(File file) {
        if(!file.exists()) return null;

        JSONObject meta;

        File metaFile = new File(file.getParentFile(), file.getName() + ".mcmeta");
        if(metaFile.exists()) meta = JSONUtil.readFile(metaFile);
        else meta = null;

        try (FileInputStream fis = new FileInputStream(file)){
            return new Texture(IOUtil.readInputStream(fis), Optional.ofNullable(meta));
        }
        catch (Exception _) {
            return null;
        }
    }

}
