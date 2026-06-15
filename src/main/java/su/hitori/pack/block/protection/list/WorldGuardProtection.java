package su.hitori.pack.block.protection.list;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import net.kyori.adventure.key.Key;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public final class WorldGuardProtection extends KeyedService {

    @Override
    public boolean isAbleToPlace(Block block, Player player) {
        return testState(block, player);
    }

    @Override
    public boolean isAbleToBreak(Block block, Player player) {
        return isAbleToPlace(block, player);
    }

    private boolean testState(Block block, Player player) {
        return WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery().testState(
                BukkitAdapter.adapt(block.getLocation()),
                WorldGuardPlugin.inst().wrapPlayer(player),
                Flags.BUILD
        );
    }

    @Override
    public Key key() {
        return Key.key("worldguard");
    }

}
