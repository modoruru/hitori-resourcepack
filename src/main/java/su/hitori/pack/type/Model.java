package su.hitori.pack.type;

import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.json.JSONArray;
import org.json.JSONObject;
import su.hitori.api.util.JSONUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @param model model itself
 * @param texturesToRemap name to texture. all textures in this map will be remapped
 */
public record Model(JSONObject model, Map<String, Texture> texturesToRemap, Optional<ModelTransform> itemFrameLikeDisplay, Optional<Tint> tint, boolean oversizedInGui) {

    /**
     * Reads .json model file
     * @param texturesFolder folder to search for textures for model
     */
    public static Model readFile(File model, File texturesFolder) {
        JSONObject modelBody = JSONUtil.readFile(model);

        JSONObject texturesBody = modelBody.isNull("textures")
                ? new JSONObject()
                : modelBody.getJSONObject("textures");

        ModelTransform modelTransform;
        JSONObject display;
        if(!modelBody.isNull("display") && !(display = modelBody.getJSONObject("display")).isNull("fixed"))
            modelTransform = createItemFrameLikeDisplay(display.getJSONObject("fixed"));
        else modelTransform = null;

        Map<String, Texture> textures = new HashMap<>();
        for (String key : texturesBody.keySet()) {
            String path = texturesBody.getString(key);
            assert path != null;

            File textureFile = new File(texturesFolder, path + ".png");
            if(!textureFile.exists()) continue;

            Texture texture = Texture.readFile(textureFile);
            if(texture != null) textures.put(key, texture);
        }

        return new Model(modelBody, textures, Optional.ofNullable(modelTransform), Optional.empty(), false);
    }

    private static ModelTransform createItemFrameLikeDisplay(JSONObject fixedDisplay) {
        ModelTransform empty = ModelTransform.EMPTY;

        Vector3f rotation = getVector3f(fixedDisplay, "rotation", empty.rotation);
        Vector3f translation = getVector3f(fixedDisplay, "translation", empty.translation);
        Vector3f scale = getVector3f(fixedDisplay, "scale", empty.scale);

        // fixup rotation
        rotation.mul(-1);
        rotation.x -= 90;

        // ..., translation
        translation.mul(1f/16f); // 0.0625
        clampVector3f(translation, -5f, 5f);

        // ... and scale
        clampVector3f(scale, -4f, 4f);
        scale.mul(1f/2f);

        fixupTranslationFromRotation(rotation, translation);

        return new ModelTransform(rotation, translation, scale);
    }

    private static void fixupTranslationFromRotation(Vector3f rotation, Vector3f translation) {
        translation.mul(1/2f);

        final float FRAME_SHIFT = 0.4375F;
        float normalizedX = ((rotation.x % 360f) + 360f) % 360f;
        int quadrant = Math.round(normalizedX / 90f) % 4;

        float temp;
        switch (quadrant) {
            case 0 -> {
                temp = translation.y;
                translation.y = -translation.z - FRAME_SHIFT;
                translation.z = temp;
            }
            case 1 -> {
                temp = translation.x;
                translation.x = translation.z;
                translation.z = -temp - FRAME_SHIFT;
                translation.y = -translation.y;
            }
            case 2 -> {
                temp = translation.y;
                translation.y = translation.z;
                translation.z = -temp - FRAME_SHIFT;
            }
            case 3 -> {
                temp = translation.x;
                translation.x = -translation.z;
                translation.z = temp - FRAME_SHIFT;
                translation.y = -translation.y;
            }
        }
    }

    private static void clampVector3f(Vector3f vector, float minimum, float maximum) {
        vector.set(
                Math.clamp(vector.x, minimum, maximum),
                Math.clamp(vector.y, minimum, maximum),
                Math.clamp(vector.z, minimum, maximum)
        );
    }

    private static Vector3f getVector3f(JSONObject object, String string, Vector3fc defaultVec) {
        if(object.isNull(string)) return new Vector3f(defaultVec);
        JSONArray array = object.getJSONArray(string);
        return new Vector3f(
                array.getFloat(0),
                array.getFloat(1),
                array.getFloat(2)
        );
    }

    public record ModelTransform(Vector3fc rotation, Vector3fc translation, Vector3fc scale) {

        public static ModelTransform EMPTY = new ModelTransform(new Vector3f(), new Vector3f(), new Vector3f(1f));

    }

}
