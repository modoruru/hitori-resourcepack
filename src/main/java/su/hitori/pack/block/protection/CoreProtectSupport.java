package su.hitori.pack.block.protection;

import org.bukkit.plugin.PluginManager;

import java.util.Optional;

public final class CoreProtectSupport {

    private final CoreProtectAPI api;

    private CoreProtectSupport(CoreProtectAPI api) {
        this.api = api;
    }

    public static Optional<CoreProtectSupport> create(PluginManager pluginManager) {
        return Optional.ofNullable(pluginManager.getPlugin("CoreProtect"))
                .map(CoreProtectAPI::new)
                .map(CoreProtectSupport::new);
    }

}
