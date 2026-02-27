package su.hitori.pack.type.blueprint.node;

import com.mojang.math.Transformation;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public final class BlockNodeData extends NodeData {

    public final BlockState blockState;

    public BlockNodeData(UUID uuid, String name, Transformation transformation, BlockState blockState) {
        super(uuid, name, transformation);
        this.blockState = blockState;
    }

    @Override
    public NodeType nodeType() {
        return NodeType.BLOCK;
    }

}
