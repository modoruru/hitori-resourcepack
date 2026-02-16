package su.hitori.pack.pose.lie;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftWorld;
import su.hitori.pack.pose.PoseService;

import java.lang.reflect.Field;
import java.util.List;

final class LyingPoseEntity extends AreaEffectCloud {

    private final Field vehicle;

    public LyingPoseEntity(Location location) {
        super(((CraftWorld) location.getWorld()).getHandle(), location.getX(), location.getY(), location.getZ());
        persist = false;
        setRadius(0);
        setDuration(Integer.MAX_VALUE);
        setNoGravity(true);
        setInvulnerable(true);
        addTag(PoseService.SEAT_TAG);
        try {
            vehicle = Entity.class.getDeclaredField("vehicle");
        }
        catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
        vehicle.setAccessible(true);
    }

    @Override
    public void tick() { }

    @Override
    protected void handlePortal() { }

    @Override
    public boolean dismountsUnderwater() { return false; }

    public void setVehicle(Entity vehicle) {
        try {
            this.vehicle.set(this, vehicle);
        }
        catch(Throwable ignored) {}

        if(vehicle.passengers.isEmpty()) vehicle.passengers = ImmutableList.of(this);
        else {
            List<Entity> list = Lists.newArrayList(vehicle.passengers);
            list.add(this);
            vehicle.passengers = ImmutableList.copyOf(list);
        }
        vehicle.gameEvent(net.minecraft.world.level.gameevent.GameEvent.ENTITY_MOUNT, this);
    }

}