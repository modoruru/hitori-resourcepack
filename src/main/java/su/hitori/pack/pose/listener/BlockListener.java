package su.hitori.pack.pose.listener;

import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import su.hitori.pack.PackModule;
import su.hitori.pack.pose.PoseService;
import su.hitori.pack.pose.seat.SeatPose;

import java.util.List;

public final class BlockListener implements Listener {

    private final PackModule packModule;

    public BlockListener(PackModule packModule) {
        this.packModule = packModule;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void blockPistonExtendEvent(BlockPistonExtendEvent event) {
        handleBlockPistonEvent(event, event.getBlocks());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void blockPistonRetractEvent(BlockPistonRetractEvent event) {
        handleBlockPistonEvent(event, event.getBlocks());
    }

    private void handleBlockPistonEvent(BlockPistonEvent event, List<Block> blocks) {
        PoseService poseService = packModule.poseService();
        for(Block block : blocks) {
            SeatPose seatPose = poseService.getSeatPoseOnBlock(block);
            if(seatPose != null) poseService.moveSeat(seatPose, event.getDirection());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void blockExplodeEvent(BlockExplodeEvent event) {
        handleExplodeEvent(event.blockList());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void entityExplodeEvent(EntityExplodeEvent event) {
        handleExplodeEvent(event.blockList());
    }

    private void handleExplodeEvent(List<Block> blocks) {
        PoseService poseService = packModule.poseService();
        for(Block block : blocks) {
            poseService.removeSeatPose(poseService.getSeatPoseOnBlock(block));
            poseService.removeLyingPose(poseService.getLyingPoseByBlock(block));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void blockFadeEvent(BlockFadeEvent event) {
        handleBlockEvent(event, event.getBlock());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void leavesDecayEvent(LeavesDecayEvent event) {
        handleBlockEvent(event, event.getBlock());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void blockBurnEvent(BlockBurnEvent event) {
        handleBlockEvent(event, event.getBlock());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void entityChangeBlockEvent(EntityChangeBlockEvent event) {
        handleBlockEvent(event, event.getBlock());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void blockBreakEvent(BlockBreakEvent event) {
        handleBlockEvent(event, event.getBlock());
    }

    private void handleBlockEvent(Cancellable event, Block block) {
        if(packModule.poseService().removeSeatPose(packModule.poseService().getSeatPoseOnBlock(block)))
            event.setCancelled(true);
    }

}