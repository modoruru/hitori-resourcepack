package su.hitori.pack.type.block;

import org.joml.Vector3f;

import java.util.function.Function;

public enum Orientation {

    FLOOR(pos -> pos),
    CEILING(
            pos -> new LocalPos(pos.x(), -pos.y(), -pos.z()),
            0f, 0f, 180f
    ),
    SIDE_UP(
            pos -> new LocalPos(-pos.x(), pos.z(), pos.y()),
            -90f, 0f, 0f
    ),
    SIDE_DOWN(
            pos -> new LocalPos(pos.x(), -pos.z(), -pos.y()),
            90f, 180f, 0f
    );

    private final Function<LocalPos, LocalPos> posWrapper;
    private final Vector3f rotationMod;

    Orientation(Function<LocalPos, LocalPos> posWrapper) {
        this(posWrapper, 0, 0, 0);
    }

    Orientation(Function<LocalPos, LocalPos> posWrapper, float rotationModX, float rotationModY, float rotationModZ) {
        this.posWrapper = posWrapper;
        this.rotationMod = new Vector3f(rotationModX, rotationModY, rotationModZ);
    }

    public LocalPos wrap(LocalPos localPos) {
        return posWrapper.apply(localPos);
    }

    public Vector3f rotationMod() {
        return rotationMod;
    }

}
