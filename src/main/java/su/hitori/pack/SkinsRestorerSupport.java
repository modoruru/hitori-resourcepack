package su.hitori.pack;

import net.skinsrestorer.api.SkinsRestorer;
import net.skinsrestorer.api.SkinsRestorerProvider;
import net.skinsrestorer.api.event.SkinApplyEvent;
import org.bukkit.entity.Player;
import su.hitori.api.Hitori;
import su.hitori.api.logging.LoggerFactory;
import su.hitori.api.util.Messages;
import su.hitori.pack.pose.PoseService;

import java.util.logging.Logger;

final class SkinsRestorerSupport {

    private static final Logger LOGGER = LoggerFactory.instance().create(SkinsRestorerSupport.class);

    private final PoseService poseService;

    SkinsRestorerSupport(PoseService poseService) {
        this.poseService = poseService;
    }

    void initialize() {
        SkinsRestorer skinsRestorer = SkinsRestorerProvider.get();
        skinsRestorer.getEventBus().subscribe(
                Hitori.instance().plugin(),
                SkinApplyEvent.class,
                event -> {
                    Player player = event.getPlayer(Player.class);
                    if(poseService.getLyingPoseByRider(player) == null && poseService.getSeatPoseByRider(player) == null) return;
                    player.sendMessage(Messages.ERROR.create(PackConfiguration.I.unableToApplySkin));
                    event.setCancelled(true);
                }
        );
        LOGGER.info("Initialized SkinsRestorer hook");
    }

}
