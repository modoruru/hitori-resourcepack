package su.hitori.pack.host;

import io.papermc.paper.event.player.PlayerServerFullCheckEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import su.hitori.api.util.Text;
import su.hitori.pack.PackConfiguration;

public final class PackListener implements Listener {

    private final PackServer packServer;

    public PackListener(PackServer packServer) {
        this.packServer = packServer;
    }

    @EventHandler
    private void onPlayerDamage(EntityDamageEvent event) {
        if(event.getEntity() instanceof Player player && player.getResourcePackStatus() != PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void onPlayerMove(PlayerMoveEvent event) {
        if(event.getPlayer().getResourcePackStatus() != PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void onResourcePack(PlayerResourcePackStatusEvent event) {
        Player player = event.getPlayer();

        var cfg = PackConfiguration.I;
        switch (event.getStatus()) {
            case DECLINED, DISCARDED -> player.kick(Text.create(cfg.allowResourcepackToPlay));
            case FAILED_DOWNLOAD, INVALID_URL, FAILED_RELOAD  -> player.kick(Text.create(cfg.errorInstallingResourcepack));
            default -> {}
        }
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        packServer.sendPack(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onPlayerServerFullCheck(PlayerServerFullCheckEvent event) {
        if(packServer.generator().isGenerating())
            event.deny(Text.create(PackConfiguration.I.resourcepackIsGenerating));
    }

}
