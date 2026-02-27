package su.hitori.pack.type.blueprint.node;

import com.mojang.math.Transformation;

import java.util.UUID;

public final class TextNodeData extends NodeData {

    public final String text;
    public final int lineWidth;
    public final int backgroundColor;
    public final float backgroundAlpha;
    public final Align align;
    public final boolean shadow, seeThrough;
    public final float baseScale;

    public TextNodeData(UUID uuid, String name, Transformation transformation, String text, int lineWidth, int backgroundColor, float backgroundAlpha, Align align, boolean shadow, boolean seeThrough, float baseScale) {
        super(uuid, name, transformation);
        this.text = text;
        this.lineWidth = lineWidth;
        this.backgroundColor = backgroundColor;
        this.backgroundAlpha = backgroundAlpha;
        this.align = align;
        this.shadow = shadow;
        this.seeThrough = seeThrough;
        this.baseScale = baseScale;
    }

    @Override
    public NodeType nodeType() {
        return NodeType.TEXT;
    }

    public enum Align {
        RIGHT, CENTER, LEFT
    }

}
