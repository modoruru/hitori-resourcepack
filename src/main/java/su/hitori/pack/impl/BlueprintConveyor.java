package su.hitori.pack.impl;

import com.mojang.brigadier.StringReader;
import com.mojang.math.Transformation;
import net.kyori.adventure.key.Key;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.CraftRegistry;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.json.JSONArray;
import org.json.JSONObject;
import su.hitori.api.logging.LoggerFactory;
import su.hitori.api.registry.RegistryKey;
import su.hitori.api.util.EnumUtil;
import su.hitori.pack.generation.GenerationContext;
import su.hitori.pack.type.blueprint.Blueprint;
import su.hitori.pack.type.blueprint.RawBlueprint;
import su.hitori.pack.type.blueprint.animation.Animation;
import su.hitori.pack.type.blueprint.animation.Frame;
import su.hitori.pack.type.blueprint.animation.LoopMode;
import su.hitori.pack.type.blueprint.node.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public final class BlueprintConveyor extends AbstractConveyorWithRegistry<RawBlueprint, Blueprint> {

    private static final Logger LOGGER = LoggerFactory.instance().create(BlueprintConveyor.class);
    private static final String ASSETS_NAMESPACE = "hitori_generated";

    public BlueprintConveyor(Key key, RegistryKey<Blueprint> registryKey) {
        super(key, registryKey);
    }

    @Override
    public void generate(GenerationContext context) {
        if(collectingOrGenerating) return;
        collectingOrGenerating = true;

        // data for generating models for Bone's
        Set<UUID> indexedModels = new HashSet<>();
        Map<UUID, byte[]> textures = new HashMap<>();
        Map<UUID, JSONObject> models = new HashMap<>();

        RawBlueprint blueprint = RawBlueprint.create(
                new File("/home/just_lofe/IdeaProjects/hitori-resourcepack/build/blueprints/example.json"),
                true
        );
        if(blueprint != null) {
            snapshots.put(blueprint.key(), blueprint);
            LOGGER.warning("blueprint not null");
        }

        registry.clear();
        snapshots.values().parallelStream().forEach(snapshot -> {
            Key key = snapshot.key();

            JSONObject blueprintBody = snapshot.body();
            JSONObject
                    settingsBody   = blueprintBody.optJSONObject("settings"),
                    texturesBody   = blueprintBody.optJSONObject("textures"),
                    nodesBody      = blueprintBody.optJSONObject("nodes"),
                    variantsBody   = blueprintBody.optJSONObject("variants"),
                    animationsBody = blueprintBody.optJSONObject("animations");

            // let's check at the start if all bodies is there
            if(settingsBody == null || texturesBody == null || nodesBody == null || variantsBody == null || animationsBody == null) {
                LOGGER.warning(String.format(
                        "Blueprint json of %s missing some of this bodies: [settings, textures, nodes, variants, animations]",
                        key.asString()
                ));
                return;
            }

            if(!settingsBody.optBoolean("baked_animations", false)) {
                LOGGER.warning(String.format(
                        "%s blueprints has unbaked animations. module supports only blueprints with baked animations.",
                        key.asString()
                ));
                return;
            }

            // let's start from the nodes
            Map<UUID, NodeData> nodes = new HashMap<>();
            for (String nodeKey : nodesBody.keySet()) {
                JSONObject nodeBody = nodesBody.optJSONObject(nodeKey);
                if(nodeBody == null) continue;

                try {
                    NodeData nodeData = createNodeData(nodeKey, nodeBody, indexedModels);
                    if(nodeData != null) nodes.put(nodeData.uuid, nodeData);
                }
                catch (Exception _) {}
            }

            Map<UUID, Animation> animations = new HashMap<>();
            for (String animationKey : animationsBody.keySet()) {
                JSONObject animationBody = animationsBody.optJSONObject(animationKey);
                if(animationBody == null) continue;

                Animation animation = createAnimation(animationKey, animationBody, indexedModels);
                if(animation != null) animations.put(animation.uuid, animation);
            }

            parseAssets(texturesBody, variantsBody, indexedModels, textures, models);

            LOGGER.warning("Registered blueprint " + key.asString());
            registry.register(
                    key,
                    new Blueprint(
                            key,
                            Optional.ofNullable(settingsBody.optJSONArray("bounding_box"))
                                    .map(array -> new Vector2i(array.getInt(0), array.getInt(1)))
                                    .orElse(null),
                            settingsBody.optInt("interpolation_duration", 1),
                            settingsBody.optInt("teleportation_duration", 1),
                            nodes,
                            animations
                    )
            );
        });

        File blueprintAssetsFolder = new File(context.folder(), "assets/" + ASSETS_NAMESPACE + "/");
        File itemsFolder = new File(blueprintAssetsFolder, "items/");
        File modelsFolder = new File(blueprintAssetsFolder, "models/item/");
        File texturesFolder = new File(blueprintAssetsFolder, "textures/item/");
        if((!itemsFolder.exists() && !itemsFolder.mkdirs()) || (!modelsFolder.exists() && !modelsFolder.mkdirs()) || (!texturesFolder.exists() && !texturesFolder.mkdirs())) {
            LOGGER.warning("Unable to create folders for generated models.");
            return;
        }

        for (Map.Entry<UUID, byte[]> textureEntry : textures.entrySet()) {
            File file = new File(texturesFolder, textureEntry.getKey() + ".png");

            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(textureEntry.getValue());
                fos.flush();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        for (Map.Entry<UUID, JSONObject> modelEntry : models.entrySet()) {
            String modelUUID = modelEntry.getKey().toString();
            File modelFile = new File(modelsFolder, modelUUID + ".json");
            try (FileWriter writer = new FileWriter(modelFile)) {
                writer.write(modelEntry.getValue().toString());
                writer.flush();
            }
            catch (IOException e) {
                e.printStackTrace();
                continue;
            }

            File itemModel = new File(itemsFolder, modelUUID + ".json");
            try (FileWriter writer = new FileWriter(itemModel)) {
                writer.write(
                        new JSONObject()
                                .put(
                                    "model",
                                        new JSONObject()
                                                .put("type", "minecraft:model")
                                                .put("model", String.format("%s:item/%s", ASSETS_NAMESPACE, modelUUID))
                                )
                                .toString()
                );
                writer.flush();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        collectingOrGenerating = false;
    }

    private static void parseAssets(JSONObject texturesBody, JSONObject variantsBody, Set<UUID> indexedModels, Map<UUID, byte[]> textures, Map<UUID, JSONObject> models) {
        Map<String, UUID> textureByKey = new HashMap<>();
        for (String textureKey : texturesBody.keySet()) {
            JSONObject textureBody = texturesBody.optJSONObject(textureKey);
            if(textureBody == null) continue;
            UUID uuid = uuidFromStringOrNull(textureBody.optString("uuid"));
            String src = textureBody.optString("src");
            if(uuid == null || src == null) continue;

            byte[] data = imageFromBase64(src);
            if(data == null) continue;

            textureByKey.put(textureKey, uuid);
            textures.put(uuid, data);
        }

        for (String variantKey : variantsBody.keySet()) {
            JSONObject variantBody = variantsBody.optJSONObject(variantKey);
            if(variantBody == null || !variantBody.optBoolean("is_default", false)) continue;

            JSONObject modelsBody = variantBody.optJSONObject("models");
            if(modelsBody == null) continue;

            for (String modelKey : modelsBody.keySet()) {
                UUID uuid = uuidFromStringOrNull(modelKey);
                JSONObject modelBody = modelsBody.optJSONObject(modelKey);
                if(uuid == null || modelBody == null || (modelBody = modelBody.optJSONObject("model")) == null || !indexedModels.contains(uuid)) continue;

                JSONObject modelTexturesBody = modelBody.optJSONObject("textures");
                if(modelTexturesBody == null) continue;

                modelBody = new JSONObject(modelBody.toMap()); // we don't want to do modifications to original json

                // fixup warning
                if(modelBody.isNull("parent"))
                    modelBody.put("parent", "item/generated");

                // remap textures
                for (String textureKey : Set.copyOf(modelTexturesBody.keySet())) {
                    UUID textureUUID = textureByKey.get(textureKey);
                    if(textureUUID == null) continue;

                    modelTexturesBody.put(textureKey, String.format(
                            "%s:item/%s",
                            ASSETS_NAMESPACE,
                            textureUUID
                    ));
                }
                modelBody.put("textures", modelTexturesBody);

                // add baked model
                models.put(uuid, modelBody);
            }
        }
    }

    private static byte[] imageFromBase64(String base64) {
        if(!base64.startsWith("data:image/png;base64,")) return null;
        return Base64.getDecoder().decode(base64.split(",", 2)[1].getBytes());
    }

    private static Animation createAnimation(String key, JSONObject animationBody, Set<UUID> indexedModels) {
        UUID uuid = uuidFromStringOrNull(key);
        String name = animationBody.optString("name", "");
        int duration = animationBody.optInt("duration", -1);
        int loopDelay = animationBody.optInt("loop_daly", 0);
        LoopMode loopMode = Optional.ofNullable(animationBody.optString("loop_mode", null))
                .map(rawLoopMode -> EnumUtil.safeValueOf(LoopMode.class, rawLoopMode))
                .orElse(null);
        if(uuid == null || duration < 0 || loopMode == null) return null;

        JSONArray framesArray = animationBody.optJSONArray("frames");
        if(framesArray == null || framesArray.isEmpty()) return null;

        Map<Integer, Frame> frames = new HashMap<>();
        for (Object rawFrame : framesArray) {
            if(!(rawFrame instanceof JSONObject frameBody)) continue;
            float time = frameBody.optFloat("time", -1);
            int timeInTicks;
            if(time == -1 || frames.containsKey(timeInTicks = (int) (time * 20))) continue;

            JSONObject nodeTransformsBody = frameBody.optJSONObject("node_transforms");
            Map<UUID, Transformation> nodeTransformations = new HashMap<>();

            for (String nodeTransformKey : nodeTransformsBody.keySet()) {
                UUID nodeUUID = uuidFromStringOrNull(nodeTransformKey);
                JSONObject nodeTransformBody = nodeTransformsBody.optJSONObject(nodeTransformKey);
                if(nodeUUID == null || nodeTransformBody == null) continue;

                Transformation transformation = Optional.ofNullable(nodeTransformBody.optJSONObject("decomposed"))
                        .map(BlueprintConveyor::createTransformation)
                        .orElse(null);
                if(transformation == null) continue;

                if(indexedModels.contains(nodeUUID)) transformation = fixupBoneTransformation(transformation);

                nodeTransformations.put(nodeUUID, transformation);
            }

            frames.put(timeInTicks, new Frame(nodeTransformations));
        }

        return new Animation(
                uuid,
                name,
                duration,
                loopDelay,
                loopMode,
                frames
        );
    }

    private static NodeData createNodeData(String key, JSONObject nodeBody, Set<UUID> indexedModels) throws RuntimeException {
        UUID uuid = uuidFromStringOrNull(key);
        Transformation transformation = Optional.ofNullable(nodeBody.optJSONObject("default_transform"))
                .map(defaultTransformBody -> defaultTransformBody.optJSONObject("decomposed"))
                .map(BlueprintConveyor::createTransformation)
                .orElse(null);
        if(uuid == null || transformation == null) return null;

        String type = nodeBody.optString("body", "bone").toLowerCase();
        String name = nodeBody.optString("name", "");

        return switch (type) {
            case "text_display" -> new TextNodeData(
                    uuid, name, transformation,
                    nodeBody.optString("text", ""),
                    nodeBody.optInt("line_width", 200),
                    Optional.ofNullable(nodeBody.optString("background_color", null)).map(str -> {
                        // argb hex starting with #
                        return Integer.parseInt(str.substring(1), 16);
                    }).orElse(0x40000000), // default background color
                    nodeBody.optFloat("background_alpha", 0.25f),
                    Optional.ofNullable(nodeBody.optString("align", null))
                            .map(rawAlign -> EnumUtil.safeValueOf(TextNodeData.Align.class, rawAlign))
                            .orElse(TextNodeData.Align.CENTER),
                    nodeBody.optBoolean("shadow", false),
                    nodeBody.optBoolean("see_through", false),
                    nodeBody.optFloat("base_scale", 1)
            );
            case "block_display" -> {
                String rawBlock = nodeBody.optString("block", null);
                if(rawBlock == null) yield null;

                BlockState state;
                try {
                    state = BlockStateParser.parseForBlock(
                            Commands.createValidationContext(CraftRegistry.getMinecraftRegistry()).lookupOrThrow(Registries.BLOCK),
                            rawBlock,
                            true
                    ).blockState();
                }
                catch (Exception _) {
                    yield null;
                }

                yield new BlockNodeData(uuid, name, transformation, state);
            }
            case "item_display" -> {
                String rawItem = nodeBody.optString("item", null);
                if(rawItem == null) yield null;

                ItemArgument argument = new ItemArgument(Commands.createValidationContext(CraftRegistry.getMinecraftRegistry()));
                ItemStack itemStack;
                try {
                    itemStack = argument.parse(new StringReader(rawItem)).createItemStack(1, true);
                }
                catch (Exception _) {
                    yield null;
                }

                yield new ItemNodeData(
                        uuid,
                        name,
                        transformation,
                        itemStack,
                        Optional.ofNullable(nodeBody.optString("item_display", null))
                                .map(String::toUpperCase)
                                .map(rawItemDisplay -> EnumUtil.safeValueOf(ItemDisplayContext.class, rawItemDisplay))
                                .orElse(ItemDisplayContext.NONE)
                );
            }
            case "locator" -> new LocatorNodeData(uuid, name, transformation);
            case "camera" -> new CameraNodeData(uuid, name, transformation);
            case "bone" -> {
                BodyNodeData bodyNodeData = Optional.ofNullable(nodeBody.optString("name", null))
                        .map(String::toUpperCase)
                        .map(rawName -> {
                            String[] unboxed = rawName.split("_", 2);

                            int characterId = 0;
                            if(unboxed.length == 2) characterId = Integer.parseInt(unboxed[1]);

                            BodyNodeType bodyNodeType = EnumUtil.safeValueOf(BodyNodeType.class, unboxed[0]);
                            if(bodyNodeType == null) return null;

                            return new BodyNodeData(uuid, name, transformation, characterId, bodyNodeType);
                        })
                        .orElse(null);
                if(bodyNodeData != null) yield bodyNodeData;

                indexedModels.add(uuid);
                yield new BoneNodeData(
                        uuid,
                        name,
                        fixupBoneTransformation(transformation),
                        new NamespacedKey(ASSETS_NAMESPACE, uuid.toString())
                );
            }
            default -> null;
        };
    }

    private static Transformation fixupBoneTransformation(Transformation boneTransformation) {
        return new Transformation(
                boneTransformation.getTranslation(),
                new Quaternionf(boneTransformation.getLeftRotation())
                        .mul(new Quaternionf(new AxisAngle4f((float) Math.toRadians(180), 0, 1, 0))),
                boneTransformation.getScale(),
                boneTransformation.getRightRotation()
        );
    }

    private static Transformation createTransformation(JSONObject decomposedNodeTransformBody) {
        JSONArray translationArray = decomposedNodeTransformBody.optJSONArray("translation");
        JSONArray leftRotationArray = decomposedNodeTransformBody.optJSONArray("left_rotation");
        JSONArray scaleArray = decomposedNodeTransformBody.optJSONArray("scale");

        if(translationArray == null || leftRotationArray == null || scaleArray == null)
            return null;

        return new Transformation(
                new Vector3f(
                        translationArray.getFloat(0),
                        translationArray.getFloat(1),
                        translationArray.getFloat(2)
                ),
                new Quaternionf(
                        leftRotationArray.getFloat(0),
                        leftRotationArray.getFloat(1),
                        leftRotationArray.getFloat(2),
                        leftRotationArray.getFloat(3)
                ),
                new Vector3f(
                        scaleArray.getFloat(0),
                        scaleArray.getFloat(1),
                        scaleArray.getFloat(2)
                ),
                new Quaternionf()
        );
    }

    private static UUID uuidFromStringOrNull(String string) {
        try {
            return UUID.fromString(string);
        }
        catch (IllegalArgumentException _) {
            return null;
        }
    }

}
