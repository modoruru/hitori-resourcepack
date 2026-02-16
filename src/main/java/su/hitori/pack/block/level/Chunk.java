package su.hitori.pack.block.level;

import su.hitori.pack.block.BlockState;
import su.hitori.pack.block.PosUtil;

import java.util.HashMap;
import java.util.Map;

final class Chunk {

    final long key;
    final Map<Long, BlockState> MAP = new HashMap<>();

    Chunk(long key) {
        this.key = key;
    }

    Chunk(long key, Map<Long, BlockState> data) {
        this.key = key;
        MAP.putAll(data);
    }

    BlockState getState(int x, int y, int z) {
        return MAP.getOrDefault(PosUtil.getBlockKey(x, y + 64, z), BlockState.EMPTY);
    }

    void setState(int x, int y, int z, BlockState blockState) {
        long key = PosUtil.getBlockKey(x, y + 64, z);
        if(blockState == null) MAP.remove(key);
        else MAP.put(key, blockState);
    }

}
