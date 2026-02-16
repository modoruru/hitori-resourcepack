package su.hitori.pack.block.protection;

import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import su.hitori.pack.block.protection.list.KeyedService;

import java.util.HashMap;
import java.util.Map;

public final class CombinedProtectionService implements ProtectionService {

    private final Map<Key, ProtectionService> child;

    public CombinedProtectionService() {
        child = new HashMap<>();
    }

    private void add(KeyedService service) {
        child.put(service.key(), service);
    }

    public void load() {
        PluginManager manager = Bukkit.getPluginManager();
        if(manager.getPlugin("WorldGuard") != null) add(new su.hitori.pack.block.protection.list.WorldGuardProtection());
    }

    @Override
    public boolean isAbleToPlace(Block block, Player player) {
        for (ProtectionService value : child.values()) {
            if(value == this || value.isAbleToPlace(block, player)) continue;
            return false;
        }
        return true;
    }

    @Override
    public boolean isAbleToBreak(Block block, Player player) {
        for (ProtectionService value : child.values()) {
            if(value == this || value.isAbleToBreak(block, player)) continue;
            return false;
        }
        return true;
    }

}
