package su.hitori.pack.type.block;

import java.util.function.Function;

public enum Direction {

    NORTH(pos -> pos, "south", -180),
    NORTH_EAST(null, "south_west", -135),
    EAST(pos -> new LocalPos(-pos.z(), pos.y(), pos.x()), "west", -90),
    EAST_SOUTH(null, "west_north", -45),
    SOUTH(pos -> new LocalPos(-pos.x(), pos.y(), -pos.z()), "north", 0),
    SOUTH_WEST(null, "north_east", 45),
    WEST(pos -> new LocalPos(-pos.z(), pos.y(), -pos.x()), "east", 90),
    WEST_NORTH(null, "east_south", 135);

    private final Function<LocalPos, LocalPos> posWrapper;
    private final String oppositeName;
    private final float yaw;

    Direction(Function<LocalPos, LocalPos> posWrapper, String oppositeName, float yaw) {
        this.posWrapper = posWrapper;
        this.oppositeName = oppositeName.toUpperCase();
        this.yaw = yaw;
    }

    public boolean subDirection() {
        return posWrapper == null;
    }

    public LocalPos wrap(LocalPos localPos) {
        if(posWrapper == null) return localPos;
        return posWrapper.apply(localPos);
    }

    public Direction opposite() {
        return Direction.valueOf(oppositeName);
    }

    public float yaw() {
        return yaw;
    }

    public float yRotationMod() {
        return -yaw;
    }

    public static Direction fromYaw(float yaw, boolean includeSubDirections) {
        yaw = (yaw % 360f + 360f) % 360f;

        int index = Math.round(yaw / 45f) % 8;

        if(!includeSubDirections) {
            return valueOf(net.minecraft.core.Direction.fromYRot(yaw).name());
        }

        return switch (index) {
            case 0 -> SOUTH;      // 0°
            case 1 -> SOUTH_WEST; // 45°
            case 2 -> WEST;       // 90°
            case 3 -> WEST_NORTH; // 135°
            case 4 -> NORTH;      // 180°
            case 5 -> NORTH_EAST; // 225° (-135°)
            case 6 -> EAST;       // 270° (-90°)
            case 7 -> EAST_SOUTH; // 315° (-45°)
            default -> SOUTH;     // не должно случиться
        };
    }

}
