package su.hitori.pack.type.block;

import java.util.Objects;

public final class LocalPos {

    private final int x, y, z;

    public LocalPos(int x, int y, int z) {
        this.x = Math.clamp(x, -8, 8);
        this.y = Math.clamp(y, -8, 8);
        this.z = Math.clamp(z, -8, 8);
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public int z() {
        return z;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof LocalPos localPos)) return false;
        return x == localPos.x
                && y == localPos.y
                && z == localPos.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

}
