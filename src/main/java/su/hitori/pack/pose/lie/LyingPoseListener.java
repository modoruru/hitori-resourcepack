package su.hitori.pack.pose.lie;

import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.MainHand;
import su.hitori.pack.pose.PoseService;

import java.util.function.Consumer;

public final class LyingPoseListener implements Listener {

    private final PoseService poseService;

    public LyingPoseListener(PoseService poseService) {
        this.poseService = poseService;
    }

    private void ifLyingPosePresent(Entity entity, Consumer<LyingPose> consumer) {
        if(!(entity instanceof Player player)) return;

        LyingPose lyingPose = poseService.getLyingPoseByRider(player);
        if(lyingPose != null) consumer.accept(lyingPose);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void playerInteractEvent(PlayerInteractEvent event) {
        ifLyingPosePresent(event.getPlayer(), _ -> event.setCancelled(true));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void playerInteractEntityEvent(PlayerInteractEntityEvent event) {
        ifLyingPosePresent(event.getPlayer(), _ -> event.setCancelled(true));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void entityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        ifLyingPosePresent(event.getDamager(), _ -> event.setCancelled(true));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void entityDamageEvent(EntityDamageEvent event) {
        ifLyingPosePresent(event.getEntity(), lyingPose -> lyingPose.playAnimation(1));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void projectileLaunchEvent(ProjectileLaunchEvent event) {
        if(event.getEntity().getShooter() instanceof Entity entity)
            ifLyingPosePresent(entity, _ -> event.setCancelled(true));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void playerAnimationEvent(PlayerAnimationEvent event) {
        if(event.getAnimationType() == PlayerAnimationType.ARM_SWING) return;
        ifLyingPosePresent(event.getPlayer(), lyingPose -> lyingPose.playAnimation(event.getPlayer().getMainHand().equals(MainHand.RIGHT) ? 0 : 3));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void inventoryClickEvent(InventoryClickEvent event) {
        var player = event.getWhoClicked();
        if(player.getGameMode() != GameMode.CREATIVE) return;
        ifLyingPosePresent(player, _ -> event.setCancelled(true));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void playerDropItemEvent(PlayerDropItemEvent event) {
        var player = event.getPlayer();
        if(player.getGameMode() != GameMode.CREATIVE) return;
        ifLyingPosePresent(player, _ -> event.setCancelled(true));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void entityPotionEffectEvent(EntityPotionEffectEvent event) {
        ifLyingPosePresent(event.getEntity(), lyingPose -> lyingPose.serverPlayer().setInvisible(true));
    }

}
