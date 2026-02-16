package su.hitori.pack.pose.listener;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.RayTraceResult;
import su.hitori.pack.pose.PoseService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class SeatListener implements Listener {

    private final PoseService poseService;
    private final Map<UUID, Long> lastActionTime;

    public SeatListener(PoseService poseService) {
        this.poseService = poseService;
        this.lastActionTime = new HashMap<>();
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event) {
        lastActionTime.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        if(event.getHand() != EquipmentSlot.HAND || action != Action.RIGHT_CLICK_BLOCK || event.getBlockFace() != BlockFace.UP || event.getItem() != null)
            return;

        Player player = event.getPlayer();
        Block clickedBlock = event.getClickedBlock();
        assert clickedBlock != null;

        Material blockType = clickedBlock.getType();

        // Various checks
        long lastActionTime = this.lastActionTime.getOrDefault(player.getUniqueId(), -1L), now = System.currentTimeMillis();
        long diff = (long) ((Math.max(lastActionTime, now) - Math.min(lastActionTime, now)) / 20d);
        if(diff < 30) return;

        if(!Tag.STAIRS.isTagged(blockType) && !Tag.SLABS.isTagged(blockType)) return;
        if(!player.isValid() || player.isSneaking()) return;
        if(poseService.isBlocked(player)) return;

        if(!clickedBlock.getRelative(BlockFace.UP).isPassable()) return;

        RayTraceResult rayTraceResult = player.rayTraceBlocks(player.getAttribute(Attribute.BLOCK_INTERACTION_RANGE).getBaseValue());
        if(rayTraceResult != null) {
            BlockFace face = rayTraceResult.getHitBlockFace();
            if(face != null && face != BlockFace.UP) return;

            Block targetBlock = rayTraceResult.getHitBlock();
            if(targetBlock != null && !clickedBlock.equals(targetBlock)) return;
        }

        if(poseService.isBlockOccupied(clickedBlock)) return;

        if(Tag.STAIRS.isTagged(blockType)) {
            if(((Stairs) clickedBlock.getBlockData()).getHalf() == Bisected.Half.BOTTOM && poseService.createStairSeatForEntity(clickedBlock, player) != null) {
                event.setCancelled(true);
                this.lastActionTime.put(player.getUniqueId(), now);
                return;
            }

            return;
        }
        if(Tag.SLABS.isTagged(blockType) && ((Slab) clickedBlock.getBlockData()).getType() != Slab.Type.BOTTOM) {
            return;
        }

        if(poseService.createSeatPose(
                clickedBlock,
                player,
                true,
                0d, 0d, 0d,
                player.getLocation().getYaw(),
                true
        ) != null) {
            event.setCancelled(true);
            this.lastActionTime.put(player.getUniqueId(), now);
        }
    }

}