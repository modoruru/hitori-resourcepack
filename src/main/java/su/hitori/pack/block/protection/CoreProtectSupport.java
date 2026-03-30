package su.hitori.pack.block.protection;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

import java.util.Optional;

public final class CoreProtectSupport {

    private final CoreProtectAPI api;

    private CoreProtectSupport(CoreProtectAPI api) {
        this.api = api;
    }

    public void logCustomBlockPlacement(Player player, Block block) {
        api.logCustomBlockPlacement(player, block);
    }

    public void logCustomBlockBreak(Player player, Block block) {
        api.logCustomBlockBreak(player, block);
    }

    public static Optional<CoreProtectSupport> create(PluginManager pluginManager) {
        return Optional.ofNullable(pluginManager.getPlugin("CoreProtect"))
                .map(CoreProtectAPI::new)
                .map(CoreProtectSupport::new);
    }

}
