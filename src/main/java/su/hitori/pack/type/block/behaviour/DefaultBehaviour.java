package su.hitori.pack.type.block.behaviour;

import org.bukkit.block.Block;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import su.hitori.pack.block.BlockState;
import su.hitori.pack.block.event.CustomBlockClickEvent;
import su.hitori.pack.type.ItemModel;
import su.hitori.pack.type.block.CustomBlock;

public record DefaultBehaviour(ItemModel model) implements BehaviourProperties {

    @Override
    public BehaviourType type() {
        return BehaviourType.DEFAULT;
    }

    @Override
    public boolean onPlayerInteract(CustomBlock customBlock, Block clickedBlock, BlockState clickedState, Block center, BlockState centerState, ItemDisplay displayEntity, Player player, EquipmentSlot hand, ItemStack handItem) {
        CustomBlockClickEvent event = new CustomBlockClickEvent(player, customBlock, clickedBlock);
        event.callEvent();
        return event.consumeInput();
    }

    @Override
    public void updateStateFromAdditionalData(CustomBlock customBlock, Block center, BlockState centerState, ItemDisplay displayEntity) {
    }

}
