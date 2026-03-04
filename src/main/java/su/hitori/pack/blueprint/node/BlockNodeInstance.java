package su.hitori.pack.blueprint.node;

import com.mojang.math.Transformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import su.hitori.pack.blueprint.BlueprintInstance;
import su.hitori.pack.type.blueprint.node.BlockNodeData;

public final class BlockNodeInstance extends NodeInstance<BlockNodeData, Display.BlockDisplay> {

    BlockNodeInstance(BlueprintInstance blueprintInstance, BlockNodeData nodeData) {
        super(blueprintInstance, nodeData);
    }

    @Override
    Display.BlockDisplay createEntity(ServerLevel serverLevel) {
        Display.BlockDisplay entity = new Display.BlockDisplay(EntityType.BLOCK_DISPLAY, serverLevel);
        entity.setBlockState(nodeData.blockState);
        entity.setTransformation(nodeData.transformation);
        return entity;
    }

    @Override
    public void applyTransformation(Transformation transformation) {
        entity.setTransformation(transformation);
    }

}
