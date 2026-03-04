package su.hitori.pack.blueprint.node;

import com.mojang.math.Transformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import su.hitori.pack.blueprint.BlueprintInstance;
import su.hitori.pack.type.blueprint.node.ItemNodeData;

public final class ItemNodeInstance extends NodeInstance<ItemNodeData, Display.ItemDisplay> {

    ItemNodeInstance(BlueprintInstance blueprintInstance, ItemNodeData nodeData) {
        super(blueprintInstance, nodeData);
    }

    @Override
    Display.ItemDisplay createEntity(ServerLevel serverLevel) {
        Display.ItemDisplay entity = new Display.ItemDisplay(EntityType.ITEM_DISPLAY, serverLevel);
        entity.setItemStack(nodeData.itemStack);
        entity.setItemTransform(nodeData.itemDisplayContext);
        entity.setTransformation(nodeData.transformation);
        return entity;
    }

    @Override
    public void applyTransformation(Transformation transformation) {
        entity.setTransformation(transformation);
    }

}
