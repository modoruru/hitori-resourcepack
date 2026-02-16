package su.hitori.pack.type.block.placement;

import org.bukkit.block.Block;
import su.hitori.pack.type.block.Direction;
import su.hitori.pack.type.block.LocalPos;
import su.hitori.pack.type.block.Orientation;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public final class SolidPlacementProperties implements PlacementProperties {

    private final OrientationProperties orientationProperties;
    private final boolean unlockedSubDirections, itemFrameLikeDisplay;
    private final Set<LocalPos> barriers;
    private final boolean survivalFriendly;

    public SolidPlacementProperties(OrientationProperties orientationProperties, boolean unlockedSubDirections, boolean itemFrameLikeDisplay, boolean survivalFriendly, Set<LocalPos> barriers) {
        this.orientationProperties = orientationProperties;
        this.unlockedSubDirections = unlockedSubDirections;
        this.itemFrameLikeDisplay = itemFrameLikeDisplay;
        this.survivalFriendly = survivalFriendly;

        // ensure barriers have center block
        Set<LocalPos> temp = new HashSet<>(barriers);
        temp.add(new LocalPos(0, 0, 0));
        for (LocalPos barrier : barriers) {
            if(barrier.y() < 0) throw new IllegalArgumentException("SolidPlacementProperties can't contain barriers with negative Y");
        }

        if(unlockedSubDirections && temp.size() > 1) throw new IllegalArgumentException("subDirections only can be unlocked without sub-barriers");

        this.barriers = Set.copyOf(temp);
    }

    @Override
    public OrientationProperties orientationProperties() {
        return orientationProperties;
    }

    @Override
    public boolean unlockedSubDirections() {
        return unlockedSubDirections;
    }

    @Override
    public boolean itemFrameLikeDisplay() {
        return itemFrameLikeDisplay;
    }

    @Override
    public boolean survivalFriendly() {
        return survivalFriendly;
    }

    public Set<LocalPos> barriers() {
        return barriers;
    }

    @Override
    public PlacementType type() {
        return PlacementType.SOLID;
    }

    @Override
    public Collection<Block> getBlocksAffectedByPlacement(Direction direction, Orientation orientation, Block center) {
        Set<Block> blocks = new HashSet<>();
        for (LocalPos barrier : barriers) {
            LocalPos mapped = direction.wrap(
                    (orientationProperties.unlocked()
                            ? orientation
                            : orientationProperties.def()
                    ).wrap(barrier)
            );
            blocks.add(center.getRelative(mapped.x(), mapped.y(), mapped.z()));
        }
        return Set.copyOf(blocks);
    }

    public static SolidPlacementProperties createCuboid(OrientationProperties orientationProperties, boolean itemFrameLikeDisplay, boolean survivalFriendly, int depth, int height, int width) {
        if(depth == 1 && height == 1 && width == 1) return new SolidPlacementProperties(
                orientationProperties,
                false,
                itemFrameLikeDisplay,
                survivalFriendly,
                Set.of(new LocalPos(0, 0, 0))
        );

        int[]
                normalizedDepth = normalizeDimension(depth, true),
                normalizedHeight = normalizeDimension(height, false),
                normalizedWidth = normalizeDimension(width, true);

        Set<LocalPos> barriers = new HashSet<>();
        for (int x : normalizedDepth) {
            for (int y : normalizedHeight) {
                for (int z : normalizedWidth) {
                    barriers.add(new LocalPos(x, y, z));
                }
            }
        }

        return new SolidPlacementProperties(orientationProperties, false, itemFrameLikeDisplay, survivalFriendly, barriers);
    }

    private static int[] normalizeDimension(int dimension, boolean mapToCenter) {
        if(dimension <= 1) return new int[]{
                0
        };
        if(dimension > 16) dimension = 16;
        if(!mapToCenter) return createArray(0, dimension - 1);

        double halfX1 = (dimension - 1) * 0.5d;
        return createArray(
                -(int) Math.ceil(halfX1),
                (int) Math.floor(halfX1)
        );
    }

    private static int[] createArray(int from, int to) {
        int length = (to - from) + 1;
        int[] array = new int[length];
        for (int i = 0; i < length; i++) {
            array[i] = from + i;
        }
        return array;
    }

}
