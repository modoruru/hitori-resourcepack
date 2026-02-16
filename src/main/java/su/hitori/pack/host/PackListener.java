package su.hitori.pack.host;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import su.hitori.api.util.Text;

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

        switch (event.getStatus()) {
            case DECLINED, DISCARDED -> player.kick(Text.create("Разрешите установку набора ресурсов для игры на сервере."));
            case FAILED_DOWNLOAD, INVALID_URL, FAILED_RELOAD  -> player.kick(Text.create("Произошла ошибка установки набора ресурсов."));
            default -> {}
        }
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        packServer.sendPack(event.getPlayer());
    }

}
