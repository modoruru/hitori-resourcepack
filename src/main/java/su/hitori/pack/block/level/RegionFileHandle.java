package su.hitori.pack.block.level;

import net.kyori.adventure.key.Key;
import su.hitori.pack.block.BlockPos;
import su.hitori.pack.block.BlockState;
import su.hitori.pack.block.PosUtil;
import su.hitori.pack.type.block.Direction;
import su.hitori.pack.type.block.Orientation;
import su.windmill.bytes.FastBytes;
import su.windmill.bytes.buffer.FastBuffer;
import su.windmill.bytes.codec.Codec;
import su.windmill.bytes.codec.context.DecodeContext;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/*
Handles file read/write for regions
 */
final class RegionFileHandle {

    private static final Codec<BlockState> BLOCK_STATE_CODEC = Codec.fixed(
            (encodable, buffer) -> {
                buffer.writeUTF8(encodable.key().asMinimalString());
                buffer.writeInt(encodable.direction().ordinal());
                buffer.writeInt(encodable.orientation().ordinal());

                BlockPos pos = encodable.parent();
                boolean present = pos != null;
                buffer.writeBoolean(present);

                if(present) {
                    buffer.writeInt(pos.x());
                    buffer.writeInt(pos.y());
                    buffer.writeInt(pos.z());
                }

                buffer.writeInt(encodable.additionalData);
            },
            ctx -> {
                FastBuffer buffer = ctx.buffer();

                String key = buffer.readUTF8();
                int direction = buffer.readInt();
                int orientation = buffer.readInt();
                BlockPos pos;
                if(buffer.readBoolean()) pos = new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt());
                else pos = null;

                return new BlockState(
                        Key.key(key),
                        Direction.values()[direction],
                        Orientation.values()[orientation],
                        pos,
                        buffer.readInt()
                );
            }
    );

    private final File file;

    private boolean read;
    private Map<Long, Map<Long, BlockState>> rawChunkData;

    RegionFileHandle(File file) {
        this.file = file;
    }

    void putChunkData(int localChunkX, int localChunkZ, Chunk chunk) {
        rawChunkData.put(
                PosUtil.getChunkKey(localChunkX, localChunkZ),
                new HashMap<>(chunk.MAP)
        );
    }

    Chunk readChunkData(int localChunkX, int localChunkZ) {
        if(!read) read();
        long key = PosUtil.getChunkKey(localChunkX, localChunkZ);

        Map<Long, BlockState> raw = rawChunkData.remove(key);
        if(raw == null) return new Chunk(key);
        return new Chunk(key, new HashMap<>(raw));
    }

    private void read() {
        if(read) return;
        rawChunkData = new HashMap<>();
        if(!file.exists()) {
            read = true;
            return;
        }

        FastBuffer buffer;
        try {
            buffer = FastBytes.readFile(file);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

        short chunks = buffer.readShort(); // chunk amount
        for (int i = 0; i < chunks; i++) {
            long key = buffer.readLong(); // chunk key;

            int blockStates = buffer.readInt();
            Map<Long, BlockState> map = new HashMap<>(blockStates);
            for (int j = 0; j < blockStates; j++) {
                long blockStateKey = buffer.readLong(); // block state key
                BlockState state = BLOCK_STATE_CODEC.decode(DecodeContext.of(buffer)); // block state
                map.put(blockStateKey, state);
            }

            rawChunkData.put(key, map);
        }

        read = true;
    }

    void save() {
        if(!read) return;
        short chunks = Integer.valueOf(rawChunkData.size()).shortValue(); // better cast
        if(chunks == 0) {
            boolean _ = file.delete();
            return;
        }

        FastBuffer buffer = FastBytes.expanding();
        buffer.writeShort(chunks); // chunk amount
        for (Map.Entry<Long, Map<Long, BlockState>> entry : rawChunkData.entrySet()) {
            buffer.writeLong(entry.getKey()); // chunk key

            Map<Long, BlockState> map = entry.getValue();
            buffer.writeInt(map.size()); // block state amount
            for (Map.Entry<Long, BlockState> stateEntry : map.entrySet()) {
                buffer.writeLong(stateEntry.getKey()); // block state key
                BLOCK_STATE_CODEC.encode(stateEntry.getValue(), buffer); // block state
            }
        }

        file.getParentFile().mkdirs();
        FastBytes.writeFile(file, buffer);
    }

}

