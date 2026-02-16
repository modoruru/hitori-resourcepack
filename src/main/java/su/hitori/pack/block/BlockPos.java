package su.hitori.pack.block;

import org.bukkit.block.Block;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

public record BlockPos(int x, int y, int z) {

    public BlockPos(Block block) {
        this(block.getX(), block.getY(), block.getZ());
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BlockPos blockPos)) return false;
        return x == blockPos.x && y == blockPos.y && z == blockPos.z;
    }

    public static boolean equals(Block first, Block second) {
        return first.getX() == second.getX()
                && first.getY() == second.getY()
                && first.getZ() == second.getZ();
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    @Override
    public @NonNull String toString() {
        return "BlockPos{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }
}
