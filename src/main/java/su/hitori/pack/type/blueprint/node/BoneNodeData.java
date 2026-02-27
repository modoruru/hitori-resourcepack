package su.hitori.pack.type.blueprint.node;

import com.mojang.math.Transformation;
import org.bukkit.NamespacedKey;

import java.util.UUID;

public final class BoneNodeData extends NodeData {

    public final NamespacedKey itemModelKey;

    public BoneNodeData(UUID uuid, String name, Transformation transformation, NamespacedKey itemModelKey) {
        super(uuid, name, transformation);
        this.itemModelKey = itemModelKey;
    }

    @Override
    public NodeType nodeType() {
        return NodeType.BONE;
    }

}
