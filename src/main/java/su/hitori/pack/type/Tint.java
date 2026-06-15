package su.hitori.pack.type;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.function.Consumer;

public final class Tint {

    private final Type type;

    private int @Nullable [] values; // for one-field types

    private float grass_temperature, grass_downfall;

    private @Nullable Integer custom_model_data_index;
    private int @Nullable [] custom_model_data_default;

    private Tint(Type type) {
        this.type = type;
    }

    @Deprecated(forRemoval = true)
    public static Tint createPotion(int defaultValue) {
        return potion(defaultValue);
    }

    public static Tint constant(int... value) {
        return createOneField(Type.CONSTANT, value);
    }

    public static Tint dye(int... defaultValue) {
        return createOneField(Type.DYE, defaultValue);
    }

    public static Tint firework(int... defaultValue) {
        return createOneField(Type.FIREWORK, defaultValue);
    }

    public static Tint grass(float temperature, float downfall) {
        return create(Type.GRASS, tint -> {
            tint.grass_temperature = temperature;
            tint.grass_downfall = downfall;
        });
    }

    public static Tint mapColor(int... defaultValue) {
        return createOneField(Type.MAP_COLOR, defaultValue);
    }

    public static Tint potion(int... defaultValue) {
        return createOneField(Type.POTION, defaultValue);
    }

    public static Tint team(int... defaultValue) {
        return createOneField(Type.TEAM, defaultValue);
    }

    public static Tint customModelData(@Nullable Integer index, int... defaultValue) {
        return create(Type.CUSTOM_MODEL_DATA, tint -> {
            tint.custom_model_data_index = index;
            tint.custom_model_data_default = defaultValue;
        });
    }

    private static Tint createOneField(Type type, int... values) {
        return create(type, tint -> tint.values = values);
    }

    private static Tint create(Type type, Consumer<Tint> setup) {
        Tint tint = new Tint(type);
        setup.accept(tint);
        return tint;
    }

    public JSONObject encode() {
        JSONObject json = new JSONObject();
        json.put("type", "minecraft:" + type.name().toLowerCase());
        switch (type) {
            case DYE, FIREWORK, MAP_COLOR, POTION, TEAM -> {
                assert values != null;

                if(values.length == 1) json.put("default", values[0]);
                else json.put("default", new JSONArray().putAll(values));
            }
            case CONSTANT -> {
                assert values != null;

                if(values.length == 1) json.put("value", values[0]);
                else json.put("value", new JSONArray().putAll(values));
            }
            case GRASS -> {
                json.put("temperature", grass_temperature);
                json.put("downfall", grass_downfall);
            }
            case CUSTOM_MODEL_DATA -> {
                if(custom_model_data_index != null)
                    json.put("index", custom_model_data_index);

                assert custom_model_data_default != null;

                if(custom_model_data_default.length == 1) json.put("default", custom_model_data_default[0]);
                else json.put("default", new JSONArray().putAll(custom_model_data_default));
            }
            default -> {}
        }
        return json;
    }

    public enum Type {
        CONSTANT,
        DYE,
        FIREWORK,
        GRASS,
        MAP_COLOR,
        POTION,
        TEAM,
        CUSTOM_MODEL_DATA
    }

}
