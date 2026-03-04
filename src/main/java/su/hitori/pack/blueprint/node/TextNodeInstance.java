package su.hitori.pack.blueprint.node;

import com.mojang.math.Transformation;
import io.papermc.paper.adventure.AdventureComponent;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import su.hitori.pack.blueprint.BlueprintInstance;
import su.hitori.pack.type.blueprint.node.TextNodeData;

public final class TextNodeInstance extends NodeInstance<TextNodeData, Display.TextDisplay> {

    TextNodeInstance(BlueprintInstance blueprintInstance, TextNodeData nodeData) {
        super(blueprintInstance, nodeData);
    }

    @Override
    Display.TextDisplay createEntity(ServerLevel serverLevel) {
        Display.TextDisplay entity = new Display.TextDisplay(EntityType.TEXT_DISPLAY, serverLevel);
        entity.setText(new AdventureComponent(JSONComponentSerializer.json().deserialize(nodeData.text)));

        var align = nodeData.align;
        setFlag(entity, 8, align == TextNodeData.Align.LEFT);
        setFlag(entity, 16, align == TextNodeData.Align.RIGHT);

        setFlag(entity, 1, nodeData.shadow);
        setFlag(entity, 2, nodeData.seeThrough);

        var entityData = entity.getEntityData();
        entityData.set(Display.TextDisplay.DATA_LINE_WIDTH_ID, nodeData.lineWidth);
        entityData.set(Display.TextDisplay.DATA_BACKGROUND_COLOR_ID, nodeData.backgroundColor);

        entity.setTransformation(nodeData.transformation);

        return entity;
    }

    private void setFlag(Display.TextDisplay entity, int flag, boolean set) {
        byte flagBits = entity.getFlags();

        if (set) flagBits = (byte) (flagBits | flag);
        else flagBits = (byte) (flagBits & ~flag);

        entity.setFlags(flagBits);
    }

    @Override
    public void applyTransformation(Transformation transformation) {
        entity.setTransformation(transformation);
    }

}
