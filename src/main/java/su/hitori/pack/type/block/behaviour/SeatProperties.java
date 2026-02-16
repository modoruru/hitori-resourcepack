package su.hitori.pack.type.block.behaviour;

import net.kyori.adventure.key.Key;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;
import su.hitori.api.Hitori;
import su.hitori.pack.PackModule;
import su.hitori.pack.block.BlockState;
import su.hitori.pack.pose.PoseService;
import su.hitori.pack.type.ItemModel;
import su.hitori.pack.type.block.CustomBlock;
import su.hitori.pack.type.block.Orientation;

public record SeatProperties(ItemModel model, float yShift) implements BehaviourProperties {

    @Override
    public BehaviourType type() {
        return BehaviourType.SEAT;
    }

    @Override
    public boolean onPlayerInteract(CustomBlock customBlock, Block clickedBlock, BlockState clickedState, Block center, BlockState centerState, ItemDisplay displayEntity, Player player, EquipmentSlot hand, ItemStack handItem) {
        if(clickedState.orientation() != Orientation.FLOOR) return false;

        PoseService poseService = Hitori.instance().moduleRepository().<PackModule>getUnsafe(Key.key("hitori", "resourcepack"))
                .map(PackModule::poseService)
                .orElse(null);
        if(poseService == null) return false;

        if(hand != EquipmentSlot.HAND || handItem != null) return false;
        if(!player.isValid() || player.isSneaking()) return false;
        if(!clickedBlock.getRelative(BlockFace.UP).isPassable()) return false;
        if(poseService.isBlocked(player) || poseService.isBlockOccupied(clickedBlock)) return false;

        RayTraceResult rayTraceResult = player.rayTraceBlocks(player.getAttribute(Attribute.BLOCK_INTERACTION_RANGE).getBaseValue());
        if(rayTraceResult != null) {
            BlockFace face = rayTraceResult.getHitBlockFace();
            if(face != null && face != BlockFace.UP) return false;

            Block targetBlock = rayTraceResult.getHitBlock();
            if(targetBlock != null && !clickedBlock.equals(targetBlock)) return false;
        }

        return poseService.createSeatPose(
                clickedBlock,
                player,
                true,
                0d,
                yShift,
                0d,
                player.getLocation().getYaw(),
                true
        ) != null;
    }

    @Override
    public void updateStateFromAdditionalData(CustomBlock customBlock, Block center, BlockState centerState, ItemDisplay displayEntity) {

    }

}
