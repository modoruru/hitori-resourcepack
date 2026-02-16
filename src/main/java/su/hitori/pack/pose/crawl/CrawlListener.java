package su.hitori.pack.pose.crawl;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleSwimEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import su.hitori.pack.pose.PoseService;

public final class CrawlListener implements Listener {

    private final PoseService poseService;

    public CrawlListener(PoseService poseService) {
        this.poseService = poseService;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onPlayerMove(PlayerMoveEvent event) {
        CrawlPose crawlPose = poseService.getCrawlPoseByCrawling(event.getPlayer());
        if(crawlPose == null) return;

        Location from = event.getFrom(), to = event.getTo();
        if(from.x() != to.x() || from.y() != to.y() || from.z() != to.z()) crawlPose.tick(to);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        if(event.isSneaking())
            poseService.removeCrawlPose(poseService.getCrawlPoseByCrawling(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityToggleSwim(EntityToggleSwimEvent event) {
        if(event.getEntity() instanceof Player player) {
            CrawlPose crawlPose = poseService.getCrawlPoseByCrawling(player);
            if(crawlPose != null) event.setCancelled(true);
        }
    }


}
