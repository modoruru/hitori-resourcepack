package su.hitori.pack.block;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.hitori.pack.type.block.Direction;
import su.hitori.pack.type.block.Orientation;

public final class BlockState implements Keyed {

    public static final BlockState EMPTY = new BlockState(Key.key("empty"), Direction.NORTH, Orientation.FLOOR, null, 0);

    private final Key key;
    private final Direction direction;
    private final Orientation orientation;
    private final BlockPos parent;
    public int additionalData;

    public BlockState(@NotNull Key key, @NotNull Direction direction, @NotNull Orientation orientation, @Nullable BlockPos parent, int additionalData) {
        this.key = key;
        this.direction = direction;
        this.orientation = orientation;
        this.parent = parent;
        this.additionalData = additionalData;
    }

    public boolean isEmpty() {
        return this == EMPTY;
    }

    @Override
    public @NotNull Key key() {
        return key;
    }

    public Direction direction() {
        return direction;
    }

    public Orientation orientation() {
        return orientation;
    }

    public @Nullable BlockPos parent() {
        return parent;
    }

    public boolean isChild() {
        return parent != null;
    }

}
