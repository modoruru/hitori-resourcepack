package su.hitori.pack.type.block.placement;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.util.BoundingBox;
import su.hitori.pack.block.level.Level;
import su.hitori.pack.type.block.Direction;
import su.hitori.pack.type.block.Orientation;

import java.util.Collection;
import java.util.List;

/**
 * Describes placement of custom blocks
 */
public sealed interface PlacementProperties permits EntityPlacementProperties, SolidPlacementProperties {

    PlacementType type();

    OrientationProperties orientationProperties();

    boolean unlockedSubDirections();

    boolean itemFrameLikeDisplay();

    boolean survivalFriendly();

    default boolean canBePlaced(Direction direction, Orientation orientation, Block center, Block ignorePlaced, Level level, boolean ignoreEntities) {
        List<BoundingBox> hitboxes;
        if(!ignoreEntities) hitboxes = center.getWorld().getEntities()
                .parallelStream()
                .filter(Entity::isValid)
                .filter(entity -> ((CraftEntity) entity).getHandle().blocksBuilding)
                .map(Entity::getBoundingBox)
                .toList();
        else hitboxes = null;

        for (Block block : getBlocksAffectedByPlacement(direction, orientation, center)) {
            if (ignorePlaced != block && block.getType() != Material.AIR) return false;
            // check if there is custom blocks, not just vanilla
            if(!level.getState(block.getX(), block.getY(), block.getZ()).isEmpty()) return false;

            if(!ignoreEntities) {
                // todo: maybe this can be optimized? idk
                AABB aabb = Shapes.block().bounds();
                BoundingBox blockHitbox = new BoundingBox(
                        (double)block.getX() + aabb.minX,
                        (double)block.getY() + aabb.minY,
                        (double)block.getZ() + aabb.minZ,
                        (double)block.getX() + aabb.maxX,
                        (double)block.getY() + aabb.maxY,
                        (double)block.getZ() + aabb.maxZ
                );
                for (BoundingBox hitbox : hitboxes) {
                    if(blockHitbox.overlaps(hitbox)) return false;
                }
            }
        }
        return true;
    }

    Collection<Block> getBlocksAffectedByPlacement(Direction direction, Orientation orientation, Block center);

    default SolidPlacementProperties asSolid() {
        if(type() == PlacementType.SOLID) return (SolidPlacementProperties) this;
        throw new IllegalStateException("type is not SOLID!");
    }

    default EntityPlacementProperties asEntity() {
        if(type() == PlacementType.ENTITY) return (EntityPlacementProperties) this;
        throw new IllegalStateException("type is not ENTITY!");
    }


}
