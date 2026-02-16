package su.hitori.pack.block;

import su.hitori.api.Pair;

public final class PosUtil {

    private static final int BLOCK_SHIFT_X = 0;
    private static final int BLOCK_SHIFT_Z = 4;
    private static final int BLOCK_SHIFT_Y = 8;

    private static final long BLOCK_MASK_XZ = 0b1111;
    private static final long BLOCK_MASK_Y = 0b1_1111_1111;

    private static final int REGION_SHIFT = 5;
    private static final int REGION_SIZE = 32, CHUNK_SIZE = 16;

    private PosUtil() {}

    public static Pair<Integer, Integer> getChunkCoordsInRegion(final int chunkX, final int chunkZ) {
        return Pair.of(
                chunkX & 0x1F,
                chunkZ & 0x1F
        );
    }

    public static long getChunkKey(int x, int z) {
        return (long)x & 4294967295L | ((long)z & 4294967295L) << 32;
    }

    public static Pair<Integer, Integer> getRegionCoordsFromChunk(int chunkX, int chunkZ) {
        return Pair.of(chunkX >> REGION_SHIFT, chunkZ >> REGION_SHIFT);
    }

    public static long getRegionKey(final int chunkX, final int chunkZ) {
        return getChunkKey(chunkX >> REGION_SHIFT, chunkZ >> REGION_SHIFT);
    }

    public static Pair<Integer, Integer> getRegionCoords(long key) {
        return getChunkCoordsFromKey(key);
    }

    // returns chunk coords in world from region coords
    public static Pair<Integer, Integer> getGlobalChunkCoordsFromLocal(int localChunkX, int localChunkZ, int regionX, int regionZ) {
        return Pair.of(
                (REGION_SIZE - localChunkX) * regionX,
                (REGION_SIZE - localChunkZ) * regionZ
        );
    }

    public static BlockPos getGlobalBlockCoords(BlockPos localPos, int chunkX, int chunkZ) {
        return new BlockPos(
                (chunkX * CHUNK_SIZE) + localPos.x(),
                localPos.y(),
                (chunkZ * CHUNK_SIZE) + localPos.z()
        );
    }

    public static Pair<Integer, Integer> getChunkCoordsFromKey(long key) {
        return Pair.of(
                (int) (key & 0xFFFFFFFFL),
                (int) (key >> 32)
        );
    }

    public static long getBlockKey(int x, int y, int z) {
        if(x < 0 || y < 0 || z < 0 || x > 15 || y > 384 || z > 15) throw new IllegalArgumentException();
        return ((long) x << BLOCK_SHIFT_X |
                (long) z << BLOCK_SHIFT_Z |
                (long) y << BLOCK_SHIFT_Y
        );
    }

    public static BlockPos getBlockCoordsFromKey(long key) {
        return new BlockPos(
                (int) ((key >> BLOCK_SHIFT_X) & BLOCK_MASK_XZ), // x
                (int) ((key >> BLOCK_SHIFT_Y) & BLOCK_MASK_Y), // y
                (int) ((key >> BLOCK_SHIFT_Z) & BLOCK_MASK_XZ) // z
        );
    }

}
