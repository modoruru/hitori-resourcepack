package su.hitori.pack.pose.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import su.hitori.pack.pose.PoseService;
import su.hitori.pack.pose.lie.LyingPose;
import su.hitori.pack.pose.seat.SeatPose;

public final class PlayerListener implements Listener {

    private final PoseService poseService;

    public PlayerListener(PoseService poseService) {
        this.poseService = poseService;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void playerQuitEvent(PlayerQuitEvent event) {
        dismount(event.getPlayer(), true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void playerTeleportEvent(PlayerTeleportEvent event) {
        dismount(event.getPlayer(), false);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void playerDeathEvent(PlayerDeathEvent event) {
        dismount(event.getEntity(), false);
    }

    private void dismount(Player player, boolean safeDismount) {
        SeatPose seatPose = poseService.getSeatPoseByRider(player);
        if(seatPose != null) poseService.removeSeatPose(seatPose, safeDismount);

        LyingPose lyingPose = poseService.getLyingPoseByRider(player);
        if(lyingPose != null) poseService.removeLyingPose(lyingPose, false);
    }

}