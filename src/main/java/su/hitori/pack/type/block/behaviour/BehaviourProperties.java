package su.hitori.pack.type.block.behaviour;

import org.bukkit.block.Block;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;
import su.hitori.pack.block.BlockState;
import su.hitori.pack.type.block.CustomBlock;

/**
 * Describes custom block behaviour and display
 */
public sealed interface BehaviourProperties permits DefaultBehaviour, LightEmitterProperties, ModelSequenceProperties, SeatProperties {

    BehaviourType type();

    boolean onPlayerInteract(CustomBlock customBlock, Block clickedBlock, BlockState clickedState, Block center, BlockState centerState, @Nullable ItemDisplay displayEntity, Player player, EquipmentSlot hand, @Nullable ItemStack handItem);

    void updateStateFromAdditionalData(CustomBlock customBlock, Block center, BlockState centerState, @Nullable ItemDisplay displayEntity);

    default DefaultBehaviour asDefault() {
        if(type() == BehaviourType.DEFAULT) return (DefaultBehaviour) this;
        throw new IllegalStateException("type is not DEFAULT!");
    }

    default LightEmitterProperties asLightEmitter() {
        if(type() == BehaviourType.LIGHT_EMITTER) return (LightEmitterProperties) this;
        throw new IllegalStateException("type is not LIGHT_EMITTER!");
    }

    default ModelSequenceProperties asModelSequence() {
        if(type() == BehaviourType.MODEL_SEQUENCE) return (ModelSequenceProperties) this;
        throw new IllegalStateException("type is not MODEL_SEQUENCE!");
    }

    default SeatProperties asSeat() {
        if(type() == BehaviourType.SEAT) return (SeatProperties) this;
        throw new IllegalStateException("type is not SEAT!");
    }

}
