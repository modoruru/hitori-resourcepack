package su.hitori.pack.block.protection;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public interface ProtectionService {

    boolean isAbleToPlace(Block block, Player player);

    boolean isAbleToBreak(Block block, Player player);

}
