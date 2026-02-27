package su.hitori.pack.type.blueprint.node;

import com.mojang.math.Transformation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public final class ItemNodeData extends NodeData {

    public final ItemStack itemStack;
    public final ItemDisplayContext itemDisplayContext;

    public ItemNodeData(UUID uuid, String name, Transformation transformation, ItemStack itemStack, ItemDisplayContext itemDisplayContext) {
        super(uuid, name, transformation);
        this.itemStack = itemStack;
        this.itemDisplayContext = itemDisplayContext;
    }

    @Override
    public NodeType nodeType() {
        return NodeType.ITEM;
    }

}
