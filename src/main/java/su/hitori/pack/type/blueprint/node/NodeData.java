package su.hitori.pack.type.blueprint.node;

import com.mojang.math.Transformation;

import java.util.UUID;

public abstract class NodeData {

    public final UUID uuid;
    public final Transformation transformation;

    public NodeData(UUID uuid, Transformation transformation) {
        this.uuid = uuid;
        this.transformation = transformation;
    }

}
