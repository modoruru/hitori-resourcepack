package su.hitori.pack.pose.crawl;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Shulker;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftWorld;

final class CrawlEntity extends Shulker {

    public CrawlEntity(Location location) {
        super(EntityType.SHULKER, ((CraftWorld) location.getWorld()).getHandle());
        setPos(location.x(), location.y(), location.z());
        persist = false;
        setInvisible(true);
        setNoGravity(true);
        setInvulnerable(true);
        setNoAi(true);
        setSilent(true);
        setAttachFace(Direction.UP);
    }

    @Override
    protected void handlePortal() {
    }

    @Override
    public boolean isAffectedByFluids() {
        return false;
    }
}
