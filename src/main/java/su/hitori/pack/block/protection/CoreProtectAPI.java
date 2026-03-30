package su.hitori.pack.block.protection;

import net.coreprotect.CoreProtect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

final class CoreProtectAPI {

    private final net.coreprotect.CoreProtectAPI api;

    CoreProtectAPI(Plugin plugin) {
        this.api = ((CoreProtect) plugin).getAPI();
    }

    void logCustomBlockPlacement(Player player, Block block) {
        Material barrier = Material.BARRIER;
        api.logPlacement(
                player.getName(),
                block.getLocation(),
                barrier,
                barrier.createBlockData()
        );
    }

    void logCustomBlockBreak(Player player, Block block) {
        Material barrier = Material.BARRIER;
        api.logRemoval(
                player.getName(),
                block.getLocation(),
                barrier,
                barrier.createBlockData()
        );
    }

}
