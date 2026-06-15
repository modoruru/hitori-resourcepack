package su.hitori.pack.blueprint.node;

import com.mojang.math.Transformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import su.hitori.pack.blueprint.BlueprintInstance;
import su.hitori.pack.type.blueprint.node.CameraNodeData;

public final class CameraNodeInstance extends NodeInstance<CameraNodeData, Display.TextDisplay> {

    CameraNodeInstance(BlueprintInstance blueprintInstance, CameraNodeData nodeData) {
        super(blueprintInstance, nodeData);
    }

    @Override
    Display.TextDisplay createEntity(ServerLevel serverLevel) {
        Display.TextDisplay entity = new Display.TextDisplay(EntityType.TEXT_DISPLAY, serverLevel);
        entity.setTransformation(nodeData.transformation);
        entity.setTransformationInterpolationDelay(0);
        return entity;
    }

    @Override
    public void applyTransformation(Transformation transformation) {
        entity.setTransformation(transformation);
    }

}
