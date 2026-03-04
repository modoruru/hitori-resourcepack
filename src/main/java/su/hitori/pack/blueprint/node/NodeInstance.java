package su.hitori.pack.blueprint.node;

import com.mojang.math.Transformation;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionfc;
import org.joml.Vector3fc;
import su.hitori.api.util.UnsafeUtil;
import su.hitori.pack.blueprint.BlueprintInstance;
import su.hitori.pack.blueprint.BlueprintUtil;
import su.hitori.pack.type.blueprint.Blueprint;
import su.hitori.pack.type.blueprint.node.NodeData;
import su.hitori.pack.type.blueprint.node.NodeType;

import java.util.List;
import java.util.UUID;

public abstract sealed class NodeInstance<NodeDataType extends NodeData, EntityType extends Entity> permits BlockNodeInstance, BodyNodeInstance, BoneNodeInstance, ItemNodeInstance, TextNodeInstance {

    static final EntityDataAccessor<Integer> POSITION_ROTATION_INTERPOLATION_DURATION_DATA = Display.ItemDisplay.DATA_POS_ROT_INTERPOLATION_DURATION_ID;

    static final EntityDataAccessor<Vector3fc>
            TRANSLATION_DATA = new EntityDataAccessor<>(11, EntityDataSerializers.VECTOR3),
            SCALE_DATA = new EntityDataAccessor<>(12, EntityDataSerializers.VECTOR3);

    static final EntityDataAccessor<Quaternionfc>
            LEFT_ROTATION = new EntityDataAccessor<>(13, EntityDataSerializers.QUATERNION),
            RIGHT_ROTATION = new EntityDataAccessor<>(14, EntityDataSerializers.QUATERNION);

    private final BlueprintInstance blueprintInstance;
    protected final NodeDataType nodeData;

    protected EntityType entity;
    protected Transformation lastTransformation;

    NodeInstance(BlueprintInstance blueprintInstance, NodeDataType nodeData) {
        this.blueprintInstance = blueprintInstance;
        this.nodeData = nodeData;

        this.lastTransformation = nodeData.transformation;
    }

    public final BlueprintInstance blueprintInstance() {
        return blueprintInstance;
    }

    public final UUID uuid() {
        return nodeData.uuid;
    }

    public final NodeDataType nodeData() {
        return nodeData;
    }

    public final EntityType entity() {
        return entity;
    }

    abstract EntityType createEntity(ServerLevel serverLevel);

    public abstract void applyTransformation(Transformation transformation);

    public void summon(ServerLevel serverLevel, Vec3 position) {
        entity = createEntity(serverLevel);
        entity.setPos(position);

        if(entity instanceof Display display) {
            Blueprint blueprint = blueprintInstance.blueprint();
            display.setTransformationInterpolationDuration(blueprint.interpolationDuration());
            var entityData = entity.getEntityData();
            entityData.set(POSITION_ROTATION_INTERPOLATION_DURATION_DATA, blueprint.teleportationDuration());
            display.setTransformationInterpolationDelay(0);
        }
    }

    public List<Packet<? super ClientGamePacketListener>> createAddPackets() {
        var createEntityPacket = BlueprintUtil.createEntityPacket(entity);
        var setEntityDataPacket = BlueprintUtil.createSetEntityDataPacket(entity, false);

        if(setEntityDataPacket == null)
            return List.of(createEntityPacket);

        return List.of(createEntityPacket, setEntityDataPacket);
    }

    public static NodeInstance<? extends NodeData, ? extends Entity> create(BlueprintInstance blueprintInstance, NodeData nodeData) {
        NodeType type = nodeData.nodeType();

        return switch (type) {
            case ITEM -> new ItemNodeInstance(blueprintInstance, UnsafeUtil.cast(nodeData));
            case BODY -> new BodyNodeInstance(blueprintInstance, UnsafeUtil.cast(nodeData));
            case BLOCK -> new BlockNodeInstance(blueprintInstance, UnsafeUtil.cast(nodeData));
            case TEXT -> new TextNodeInstance(blueprintInstance, UnsafeUtil.cast(nodeData));
            case BONE -> new BoneNodeInstance(blueprintInstance, UnsafeUtil.cast(nodeData));
            default -> null;
        };
    }

}
