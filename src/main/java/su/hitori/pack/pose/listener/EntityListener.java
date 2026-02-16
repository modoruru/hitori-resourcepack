package su.hitori.pack.pose.listener;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.event.entity.EntityMountEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import su.hitori.pack.PackModule;
import su.hitori.pack.pose.PoseService;
import su.hitori.pack.pose.seat.SeatPose;

public final class EntityListener implements Listener {

    private static final NamespacedKey SEAT_TAG = new NamespacedKey("resourcepack", PoseService.SEAT_TAG);

    private final PackModule packModule;

    public EntityListener(PackModule packModule) {
        this.packModule = packModule;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void entityMountEventLow(EntityMountEvent event) {
        if(!(event.getEntity() instanceof Player player)) return;
        if(!event.getMount().getScoreboardTags().contains(PoseService.SEAT_TAG)) return;

        player.getPersistentDataContainer().set(SEAT_TAG, PersistentDataType.BOOLEAN, true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void entityMountEventHigh(EntityMountEvent event) {
        if(!(event.getEntity() instanceof Player player)) return;
        if(!event.getMount().getScoreboardTags().contains(PoseService.SEAT_TAG)) return;

        PersistentDataContainer data = player.getPersistentDataContainer();
        if(!data.has(SEAT_TAG)) return;

        data.remove(SEAT_TAG);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void entityDismountEvent(EntityDismountEvent event) {
        if(!(event.getEntity() instanceof Player player)) return;

        SeatPose seatPose = packModule.poseService().getSeatPoseByRider(player);
        if(seatPose != null) {
            packModule.poseService().removeSeatPose(seatPose, true);
            event.setCancelled(true);
        }
    }

}