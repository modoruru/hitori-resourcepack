package su.hitori.pack.impl;

import net.kyori.adventure.key.Key;
import org.json.JSONArray;
import org.json.JSONObject;
import su.hitori.api.util.FileUtil;
import su.hitori.api.util.JSONUtil;
import su.hitori.pack.generation.GenerationContext;
import su.hitori.pack.type.Sound;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;

public final class SoundConveyor extends AbstractConveyor<Sound> {

    public SoundConveyor(Key key) {
        super(key);
    }

    @Override
    public void generate(GenerationContext context) {
        if(collectingOrGenerating) return;
        collectingOrGenerating = true;

        for (Map.Entry<Key, Sound> entry : snapshots.entrySet()) {
            Key key = entry.getKey();
            Sound sound = entry.getValue();
            String path = key.value().replace('.', '/');

            File namespaceFolder = new File(
                    context.folder(),
                    "assets/" + key.namespace() + "/"
            );
            namespaceFolder.mkdirs();

            File soundsFile = new File(
                    namespaceFolder,
                    "sounds.json"
            );

            JSONObject soundsBody = JSONUtil.readFile(soundsFile)
                    .put(
                            key.value(),
                            new JSONObject()
                                    .putOpt("subtitle", sound.subtitleTranslatableKey().orElse(null))
                                    .put("sounds", new JSONArray().put(key.namespace() + ":" + path))
                    );

            FileUtil.writeTextToFile(soundsFile, soundsBody.toString());

            File soundFile = new File(namespaceFolder, "sounds/" + path + ".ogg");
            soundFile.getParentFile().mkdirs();
            try (FileOutputStream fos = new FileOutputStream(soundFile)) {
                fos.write(sound.opusData());
                fos.flush();
            }
            catch (Exception e) {
                e.printStackTrace();
                continue;
            }
        }

        collectingOrGenerating = false;
    }

}
