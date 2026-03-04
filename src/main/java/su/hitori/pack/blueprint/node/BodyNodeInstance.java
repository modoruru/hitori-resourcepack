package su.hitori.pack.blueprint.node;

import com.mojang.authlib.GameProfile;
import com.mojang.math.Transformation;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import org.joml.Vector3f;
import su.hitori.pack.blueprint.BlueprintInstance;

import su.hitori.pack.type.blueprint.node.BodyNodeData;

public final class BodyNodeInstance extends NodeInstance<BodyNodeData, Display.ItemDisplay> {

    BodyNodeInstance(BlueprintInstance blueprintInstance, BodyNodeData nodeData) {
        super(blueprintInstance, nodeData);
    }

    @Override
    Display.ItemDisplay createEntity(ServerLevel serverLevel) {
        Display.ItemDisplay entity = new Display.ItemDisplay(EntityType.ITEM_DISPLAY, serverLevel);

        ItemStack itemStack = new ItemStack(Items.PLAYER_HEAD);
        itemStack.applyComponents(
                DataComponentPatch.builder()
                        .set(DataComponents.ITEM_MODEL, Identifier.fromNamespaceAndPath(
                                "hitori_body",
                                switch (nodeData.bodyNodeType) {
                                    case HEAD -> "head";
                                    case RIGHT_ARM -> "right_arm";
                                    case LEFT_ARM -> "left_arm";
                                    case TORSO -> "torso";
                                    case RIGHT_LEG -> "right_leg";
                                    case LEFT_LEG -> "left_leg";
                                    case RIGHT_FOREARM -> "right_forearm";
                                    case LEFT_FOREARM -> "left_forearm";
                                    case WAIST -> "waist";
                                    case LOWER_RIGHT_LEG -> "lower_right_leg";
                                    case LOWER_LEFT_LEG -> "lower_left_leg";
                                }
                        ))
                        .build()
        );
        entity.setItemTransform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND);
        entity.setItemStack(itemStack);

        applyTransformation(entity, nodeData.transformation);
        return entity;
    }

    public void assignSkin(GameProfile gameProfile) {
        ItemStack stack = entity.getItemStack();
        stack.applyComponents(
                DataComponentPatch.builder()
                        .set(DataComponents.PROFILE, ResolvableProfile.createResolved(gameProfile))
                        .build()
        );
        entity.setItemStack(stack);
    }

    public void applyTransformation(Transformation transformation) {
        applyTransformation(entity, transformation);
    }

    public void applyTransformation(Display.ItemDisplay entity, Transformation transformation) {
        var entityData = entity.getEntityData();
        entityData.set(TRANSLATION_DATA, new Vector3f(transformation.getTranslation()).add(0, nodeData.bodyNodeType.negativeVerticalOffset, 0));
        entityData.set(SCALE_DATA, transformation.getScale());
        entityData.set(LEFT_ROTATION, transformation.getLeftRotation());
        entityData.set(RIGHT_ROTATION, transformation.getRightRotation());
    }

}
