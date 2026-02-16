package su.hitori.pack.pose;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftEntity;
import su.hitori.api.logging.LoggerFactory;
import su.hitori.pack.pose.seat.SeatPoseEntity;

import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

final class PoseUtil {

    private static final Logger LOGGER = LoggerFactory.instance().create(PoseUtil.class);

    private static Field entityManagerField;

    static {
        Field entityManagerField = null;
        for (Field field : ServerLevel.class.getDeclaredFields()) {
            if(field.getType().isAssignableFrom(PersistentEntitySectionManager.class)) {
                entityManagerField = field;
                entityManagerField.setAccessible(true);
                break;
            }
        }
        PoseUtil.entityManagerField = entityManagerField;
    }

    private PoseUtil() {}

    public static void setEntityLocation(org.bukkit.entity.Entity entity, Location location) {
        ((CraftEntity) entity).getHandle().absSnapTo(
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getYaw(),
                location.getPitch()
        );
    }

    public static org.bukkit.entity.Entity createSeatEntity(Location location, org.bukkit.entity.Entity rider, boolean canRotate) {
        if(rider == null || !rider.isValid()) return null;

        Entity nmsRider = ((CraftEntity) rider).getHandle();
        SeatPoseEntity seatPoseEntity = new SeatPoseEntity(location);

        if(!spawnEntity(seatPoseEntity)) return null;
        else if(!nmsRider.startRiding(seatPoseEntity, true, true) || !seatPoseEntity.passengers.contains(nmsRider)) {
            seatPoseEntity.discard();
            return null;
        }

        if(canRotate) seatPoseEntity.startRotate();

        return seatPoseEntity.getBukkitEntity();
    }

    private static boolean spawnEntity(Entity entity) {
        if(entityManagerField != null) {
            try {
                PersistentEntitySectionManager<Entity> entityLookup = (PersistentEntitySectionManager<Entity>) entityManagerField.get(entity.level().getWorld().getHandle());
                return entityLookup.addNewEntity(entity);
            } catch(Throwable e) {
                LOGGER.log(Level.SEVERE, "Could not spawn entity", e);
            }
            return false;
        }
        try {
            LevelEntityGetter<Entity> levelEntityGetter = entity.level().getEntities();
            return (boolean) levelEntityGetter.getClass().getMethod("addNewEntity", Entity.class).invoke(levelEntityGetter, entity);
        } catch(Throwable e) {
            LOGGER.log(Level.SEVERE, "Could not spawn entity", e);
        }
        return false;
    }

}
