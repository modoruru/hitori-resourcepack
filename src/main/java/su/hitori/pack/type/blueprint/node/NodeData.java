package su.hitori.pack.type.blueprint.node;

import com.mojang.math.Transformation;

import java.util.UUID;

public abstract sealed class NodeData permits BlockNodeData, BodyNodeData, BoneNodeData, CameraNodeData, ItemNodeData, LocatorNodeData, TextNodeData {

    public final UUID uuid;
    public final String name;
    public final Transformation transformation;

    public NodeData(UUID uuid, String name, Transformation transformation) {
        this.uuid = uuid;
        this.name = name;
        this.transformation = transformation;
    }

    public abstract NodeType nodeType();

}
