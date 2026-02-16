package su.hitori.pack.type;

import org.json.JSONObject;

// todo: support for other tint types
public final class Tint {

    private final Type type;

    private int constant_value;
    private int dye_default;
    private int firework_default;
    private float grass_temperature, grass_downfall;
    private int map_color_default;
    private int potion_default;
    private int team_default;

    private int custom_model_data_index;
    private int[] custom_model_data_default;

    private Tint(Type type) {
        this.type = type;
    }

    public static Tint createPotion(int _default) {
        Tint tint = new Tint(Type.POTION);
        tint.potion_default = _default;
        return tint;
    }

    public JSONObject encode() {
        JSONObject json = new JSONObject();
        json.put("type", "minecraft:" + type.name().toLowerCase());
        switch (type) {
            case POTION -> json.put("default", potion_default);
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
