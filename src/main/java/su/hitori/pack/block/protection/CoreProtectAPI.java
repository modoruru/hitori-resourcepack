package su.hitori.pack.block.protection;

import net.coreprotect.CoreProtect;
import org.bukkit.plugin.Plugin;

final class CoreProtectAPI {

    private final net.coreprotect.CoreProtectAPI api;

    public CoreProtectAPI(Plugin plugin) {
        this.api = ((CoreProtect) plugin).getAPI();
    }

}
