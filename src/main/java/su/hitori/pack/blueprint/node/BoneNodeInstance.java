package su.hitori.pack.blueprint.node;

import com.mojang.math.Transformation;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import su.hitori.pack.blueprint.BlueprintInstance;
import su.hitori.pack.type.blueprint.node.BoneNodeData;

public final class BoneNodeInstance extends NodeInstance<BoneNodeData, Display.ItemDisplay> {

    BoneNodeInstance(BlueprintInstance blueprintInstance, BoneNodeData nodeData) {
        super(blueprintInstance, nodeData);
    }

    @Override
    Display.ItemDisplay createEntity(ServerLevel serverLevel) {
        Display.ItemDisplay entity = new Display.ItemDisplay(EntityType.ITEM_DISPLAY, serverLevel);

        ItemStack itemStack = new ItemStack(Items.POPPED_CHORUS_FRUIT);
        itemStack.applyComponents(
                DataComponentPatch.builder()
                        .set(DataComponents.ITEM_MODEL, Identifier.parse(nodeData.itemModelKey.asString()))
                        .build()
        );
        entity.setItemStack(itemStack);

        entity.setTransformation(nodeData.transformation);
        return entity;
    }

    @Override
    public void applyTransformation(Transformation transformation) {
        entity.setTransformation(transformation);
    }

}
