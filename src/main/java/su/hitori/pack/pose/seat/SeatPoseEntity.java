package su.hitori.pack.pose.seat;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftWorld;
import org.jspecify.annotations.NonNull;
import su.hitori.pack.pose.PoseService;

public final class SeatPoseEntity extends ArmorStand {

    private boolean rotate;
    public Runnable runnable;

    public SeatPoseEntity(Location location) {
        super(
                ((CraftWorld) location.getWorld()).getHandle(),
                location.x(),
                location.y(),
                location.z()
        );

        persist = false;
        setInvisible(true);
        setNoGravity(true);
        setMarker(true);
        setInvulnerable(true);
        setSmall(true);
        setNoBasePlate(true);
        setRot(location.getYaw(), location.getPitch());
        yRotO = getYRot();
        setYBodyRot(yRotO);
        getAttribute(Attributes.MAX_HEALTH).setBaseValue(1f);
        addTag(PoseService.SEAT_TAG);
    }

    public void startRotate() {
        rotate = true;
    }

    @Override
    public void tick() {
        if(runnable != null) runnable.run();
        if(!isAlive() || !valid || !rotate) return;
        Entity rider = getFirstPassenger();
        if(rider == null) return;

        setYRot(rider.getYRot());
        yRotO = getYRot();
    }

    @Override
    public void move(@NonNull MoverType type, @NonNull Vec3 movement) {
    }

    @Override
    public boolean hurtServer(@NonNull ServerLevel level, @NonNull DamageSource damageSource, float amount) {
        return false;
    }

    @Override
    protected void handlePortal() {
    }

    @Override
    public boolean isAffectedByFluids() {
        return false;
    }

    @Override
    public boolean dismountsUnderwater() {
        return false;
    }

}
