package su.hitori.pack.type.block.behaviour;

import org.bukkit.block.Block;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import su.hitori.pack.block.BlockState;
import su.hitori.pack.block.level.LevelService;
import su.hitori.pack.exception.MisconfigurationException;
import su.hitori.pack.type.ItemModel;
import su.hitori.pack.type.Model;
import su.hitori.pack.type.block.CustomBlock;

import java.util.List;

public record ModelSequenceProperties(List<ItemModel> sequence) implements BehaviourProperties {

    public ModelSequenceProperties {
        if(sequence.isEmpty()) throw new MisconfigurationException("At least one model should be present in sequence");
    }

    @Override
    public BehaviourType type() {
        return BehaviourType.MODEL_SEQUENCE;
    }

    @Override
    public boolean onPlayerInteract(CustomBlock customBlock, Block clickedBlock, BlockState clickedState, Block center, BlockState centerState, ItemDisplay displayEntity, Player player, EquipmentSlot hand, ItemStack handItem) {
        int index = centerState.additionalData + 1;
        if(index >= sequence.size()) index = 0;
        centerState.additionalData = index;
        return true;
    }

    @Override
    public void updateStateFromAdditionalData(CustomBlock customBlock, Block center, BlockState centerState, ItemDisplay displayEntity) {
        ItemStack stack = displayEntity.getItemStack();
        ItemModel itemModel = sequence.get(centerState.additionalData);
        stack.editMeta(meta -> meta.setItemModel(itemModel.resolve()));
        displayEntity.setTransformationMatrix(LevelService.createPose(
                centerState.orientation(),
                centerState.direction(),
                customBlock.blockProperties().placementProperties().itemFrameLikeDisplay()
                        ? itemModel.modelTransform().orElse(Model.ModelTransform.EMPTY)
                        : Model.ModelTransform.EMPTY
        ));
        displayEntity.setItemStack(stack);
    }

}
