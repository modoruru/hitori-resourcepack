package su.hitori.pack.block.level;

import net.kyori.adventure.key.Key;
import net.minecraft.server.level.ServerLevel;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import su.hitori.pack.block.BlockState;
import su.hitori.pack.block.PosUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public final class Level {

    private final Key key;
    final Map<Long, Region> REGIONS = new HashMap<>();

    private World bukkit;

    public Level(Key key) {
        this.key = key;
    }

    private Chunk getChunk(int x, int z) {
        long key = PosUtil.getRegionKey(x, z);
        Region region = REGIONS.get(key);
        if(region == null) {
            var coords = PosUtil.getRegionCoordsFromChunk(x, z);
            int rX = coords.first();
            int rZ = coords.second();
            REGIONS.put(key, region = new Region(key, new File(getWorld().getWorldFolder(), String.format("hitori/%s.%s.rg", rX, rZ))));
        }
        return region.getChunk(x, z);
    }

    public void loadChunk(int x, int z) {
        getChunk(x, z); // force chunk to load
    }

    public void unloadChunk(int x, int z, boolean save) {
        long key = PosUtil.getRegionKey(x, z);
        Region region = REGIONS.get(key);

        if(region != null && region.unloadChunk(x, z, save))
            REGIONS.remove(key);
    }

    public BlockState getState(int x, int y, int z) {
        final int chunkX = x >> 4, chunkZ = z >> 4;
        return getChunk(chunkX, chunkZ).getState(x & 0xF, y, z & 0xF);
    }

    public void setState(int x, int y, int z, BlockState state) {
        final int chunkX = x >> 4, chunkZ = z >> 4;
        getChunk(chunkX, chunkZ).setState(x & 0xF, y, z & 0xF, state);
    }

    public void save() {
        REGIONS.values().forEach(Region::save);
    }

    public void unload() {
        REGIONS.values().forEach(Region::unload);
        REGIONS.clear();
    }

    public ServerLevel getServerLevel() {
        return ((CraftWorld) getWorld()).getHandle();
    }

    public World getWorld() {
        if(bukkit == null)
            return bukkit = Bukkit.getWorld(key);
        return bukkit;
    }

}
