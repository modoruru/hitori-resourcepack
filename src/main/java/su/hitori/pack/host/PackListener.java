package su.hitori.pack.host;

import io.papermc.paper.connection.PlayerConfigurationConnection;
import io.papermc.paper.event.connection.configuration.AsyncPlayerConnectionConfigureEvent;
import io.papermc.paper.event.player.PlayerServerFullCheckEvent;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.kyori.adventure.resource.ResourcePackStatus;
import net.kyori.adventure.text.Component;
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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public final class PackListener implements Listener {

    private final PackConfiguration configuration;
    private final PackServer packServer;

    public PackListener(PackConfiguration configuration, PackServer packServer) {
        this.configuration = configuration;
        this.packServer = packServer;
    }

    @EventHandler
    private void onPlayerDamage(EntityDamageEvent event) {
        if(configuration.restrictActionsWhenLoadingPackDuringPlay.get() && event.getEntity() instanceof Player player && player.getResourcePackStatus() != PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void onPlayerMove(PlayerMoveEvent event) {
        if(configuration.restrictActionsWhenLoadingPackDuringPlay.get() && event.getPlayer().getResourcePackStatus() != PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onConfigure(AsyncPlayerConnectionConfigureEvent event) {
        if(!configuration.sendPackDuringConfigurationPhase.get()) return;

        PlayerConfigurationConnection connection = event.getConnection();
        var infoOpt = packServer.createInfo();
        if(infoOpt.isEmpty()) {
            connection.disconnect(Text.create(configuration.resourcepackIsGenerating.get()));
            return;
        }

        CompletableFuture<ResourcePackStatus> future = new CompletableFuture<>();
        connection.getAudience().sendResourcePacks(
                ResourcePackRequest.resourcePackRequest()
                        .packs(infoOpt.get())
                        .replace(true)
                        .required(true)
                        .callback((_, status, _) -> {
                            if(status.intermediate()) return;
                            future.complete(status);
                        })
                        .build()
        );

        ResourcePackStatus status;
        try {
            status = future.orTimeout(3, TimeUnit.MINUTES).get();
        }
        catch (Throwable throwable) {
            connection.disconnect(Component.text(String.format(
                    "Resource pack downloading time limit has exceeded or an error occurred: %s(%s)",
                    throwable.getClass().getSimpleName(),
                    throwable.getMessage()
            )));
            return;
        }

        Component kickMessage = packServer.createKickMessage(status);
        if(kickMessage != null) connection.disconnect(kickMessage);
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        if(configuration.sendPackDuringConfigurationPhase.get()) return;

        Player player = event.getPlayer();
        var infoOpt = packServer.createInfo();
        if(infoOpt.isEmpty()) {
            player.kick(Text.create(configuration.resourcepackIsGenerating.get()));
            return;
        }

        player.sendResourcePacks(
                ResourcePackRequest.resourcePackRequest()
                        .replace(true)
                        .required(true)
                        .packs(infoOpt.get())
                        .callback((_, status, _) -> {
                            Component kickMessage = packServer.createKickMessage(status);
                            if(kickMessage != null) player.kick(kickMessage);
                        })
                        .build()
        );
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onPlayerServerFullCheck(PlayerServerFullCheckEvent event) {
        if(packServer.generator().isGenerating())
            event.deny(Text.create(configuration.resourcepackIsGenerating.get()));
    }

}
