package su.hitori.pack.block.level;

import su.hitori.pack.block.PosUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

final class Region {

    final long key;
    final Map<Long, Chunk> CHUNKS = new HashMap<>();
    private final RegionFileHandle regionFileHandle;

    Region(long key, File file) {
        this.key = key;
        this.regionFileHandle = new RegionFileHandle(file);
    }

    Chunk getChunk(int chunkX, int chunkZ) {
        var localCoords = PosUtil.getChunkCoordsInRegion(chunkX, chunkZ);
        long key = PosUtil.getChunkKey(localCoords.first(), localCoords.second());

        Chunk chunk = CHUNKS.get(key);
        if(chunk == null) {
            chunk = regionFileHandle.readChunkData(localCoords.first(), localCoords.second());
            CHUNKS.put(key, chunk);
        }
        return chunk;
    }

    boolean unloadChunk(int chunkX, int chunkZ, boolean save) {
        var localCoords = PosUtil.getChunkCoordsInRegion(chunkX, chunkZ);
        long key = PosUtil.getChunkKey(localCoords.first(), localCoords.second());

        Chunk chunk = CHUNKS.remove(key);
        boolean empty = CHUNKS.isEmpty();
        if(chunk == null || !save) return empty;
        regionFileHandle.putChunkData(localCoords.first(), localCoords.second(), chunk);

        if(empty) regionFileHandle.save();

        return empty;
    }

    void save() {
        for (Map.Entry<Long, Chunk> entry : CHUNKS.entrySet()) {
            var coords = PosUtil.getChunkCoordsFromKey(entry.getKey());
            regionFileHandle.putChunkData(coords.first(), coords.second(), entry.getValue());
        }
        regionFileHandle.save();
    }

    void unload() {
        save();
        CHUNKS.clear();
    }

}
