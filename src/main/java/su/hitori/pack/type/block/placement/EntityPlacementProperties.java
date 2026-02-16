package su.hitori.pack.type.block.placement;

import org.bukkit.Location;
import org.bukkit.block.Block;
import su.hitori.pack.type.block.Direction;
import su.hitori.pack.type.block.Orientation;

import java.util.Collection;
import java.util.List;

public record EntityPlacementProperties(OrientationProperties orientationProperties, boolean unlockedSubDirections, boolean itemFrameLikeDisplay, boolean survivalFriendly, double size) implements PlacementProperties {

    public Location getPositionForEntity(Direction direction, Orientation orientation, Location origin) {
        switch (orientation) {
            case FLOOR -> {
                return origin;
            }
            case CEILING -> {
                return origin.clone().add(0, 1 - size, 0);
            }
            default -> {}
        }

        double offset = (1 - size) * 0.5;
        double xOffset = 0, zOffset = 0;
        switch (direction) {
            case NORTH -> zOffset -= offset;
            case SOUTH -> zOffset += offset;
            case WEST -> xOffset -= offset;
            case EAST -> xOffset += offset;
        }

        return origin.clone().add(xOffset, offset, zOffset);
    }

    @Override
    public PlacementType type() {
        return PlacementType.ENTITY;
    }

    @Override
    public Collection<Block> getBlocksAffectedByPlacement(Direction direction, Orientation orientation, Block center) {
        return List.of(center);
    }

}
