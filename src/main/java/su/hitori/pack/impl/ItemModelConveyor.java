package su.hitori.pack.impl;

import net.kyori.adventure.key.Key;
import org.bukkit.NamespacedKey;
import org.json.JSONArray;
import org.json.JSONObject;
import su.hitori.api.util.FileUtil;
import su.hitori.pack.generation.GenerationContext;
import su.hitori.pack.type.ItemModel;
import su.hitori.pack.type.Model;
import su.hitori.pack.type.Texture;
import su.hitori.pack.type.Tint;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public final class ItemModelConveyor extends AbstractConveyor<ItemModel> {

    public ItemModelConveyor(Key key) {
        super(key);
    }

    @Override
    public void generate(GenerationContext context) {
        if(collectingOrGenerating) return;
        collectingOrGenerating = true;

        for (ItemModel value : snapshots.values()) {
            Optional<Model> optModel = value.directItemModelOrModel().secondOptional();
            if(optModel.isEmpty()) continue;

            Key key = value.key();
            Model model = optModel.get();

            JSONObject modelBody = new JSONObject(model.model().toMap());
            JSONObject texturesBody = Optional.ofNullable(modelBody.optJSONObject("textures"))
                    .orElseGet(JSONObject::new);

            File texturesFolder = new File(
                    context.folder(),
                    "assets/hitori/textures/"
            );
            texturesFolder.mkdirs();

            // remap textures
            Map<String, Texture> texturesToRemap = model.texturesToRemap();
            for (Map.Entry<String, Texture> textureEntry : texturesToRemap.entrySet()) {
                String name = textureEntry.getKey();
                String path = texturesBody.getString(name);
                if(path == null) continue;

                if(path.contains(":")) path = path.split(":", 2)[1]; // remove namespace

                File textureFile = new File(
                        texturesFolder,
                        "item/" + key.namespace() + "/" + path + ".png"
                );
                textureFile.getParentFile().mkdirs();

                Texture texture = textureEntry.getValue();
                byte[] textureData = texture.data();

                // don't override if size is the same
                // TODO: create more proper check of texture equality
                if(textureFile.length() != textureData.length) {
                    try (FileOutputStream fos = new FileOutputStream(textureFile)) {
                        fos.write(textureData);
                        fos.flush();
                    }
                    catch (IOException e) {
                        continue;
                    }
                }

                // write meta file
                texture.meta().ifPresent(meta -> {
                    File metaFile = new File(textureFile.getParentFile(), textureFile.getName() + ".mcmeta");
                    FileUtil.writeTextToFile(metaFile, meta.toString());
                });

                texturesBody.put(name, String.format(
                        "hitori:item/%s/%s",
                        key.namespace(),
                        path
                ));
            }

            if(!texturesToRemap.isEmpty()) modelBody.put("textures", texturesBody); // update textures body

            // write model
            File modelFile = new File(
                    context.folder(),
                    "assets/hitori/models/item/" + key.namespace() + "/" + key.value() + ".json"
            );
            modelFile.getParentFile().mkdirs();
            FileUtil.writeTextToFile(modelFile, modelBody.toString());

            NamespacedKey resolved = value.resolve();
            writeItemModel(
                    new File(
                            context.folder(),
                            String.format(
                                    "assets/%s/items/%s.json",
                                    resolved.namespace(),
                                    resolved.value()
                            )
                    ),
                    new NamespacedKey("hitori", "item/" + key.namespace() + "/" + key.value()),
                    model.tint().orElse(null),
                    model.oversizedInGui()
            );
        }

        collectingOrGenerating = false;
    }

    private static void writeItemModel(File file, NamespacedKey model, Tint tint, boolean oversizedInGui) {
        JSONObject modelBody = new JSONObject()
                .put("type", "minecraft:model")
                .put("model", model.asString());
        if(tint != null) {
            modelBody.put(
                    "tints",
                    new JSONArray().put(tint.encode())
            );
        }

        file.getParentFile().mkdirs();
        FileUtil.writeTextToFile(
                file,
                new JSONObject()
                        .put("oversized_in_gui", oversizedInGui)
                        .put("model", modelBody)
                        .toString()
        );
    }

}
