package su.hitori.pack.type.blueprint.node;

import com.mojang.math.Transformation;

import java.util.UUID;

public final class CameraNodeData extends NodeData {

    public CameraNodeData(UUID uuid, String name, Transformation transformation) {
        super(uuid, name, transformation);
    }

    @Override
    public NodeType nodeType() {
        return NodeType.CAMERA;
    }

}
