package su.hitori.pack.type.blueprint.node;

import com.mojang.math.Transformation;

import java.util.UUID;

public final class BodyNodeData extends NodeData {

    public final int characterId;
    public final BodyNodeType bodyNodeType;

    public BodyNodeData(UUID uuid, String name, Transformation transformation, int characterId, BodyNodeType bodyNodeType) {
        super(uuid, name, transformation);
        this.characterId = characterId;
        this.bodyNodeType = bodyNodeType;
    }

    @Override
    public NodeType nodeType() {
        return NodeType.BODY;
    }

}
