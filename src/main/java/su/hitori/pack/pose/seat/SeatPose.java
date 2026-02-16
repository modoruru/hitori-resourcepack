package su.hitori.pack.pose.seat;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public final class SeatPose {

    private final Entity seatEntity;
    private final Player rider;
    private final double initialYOffset;

    private Block block;
    private Location location;

    public SeatPose(Entity seatEntity, Player rider, double initialYOffset, Block block, Location location) {
        this.seatEntity = seatEntity;
        this.rider = rider;
        this.initialYOffset = initialYOffset;

        this.block = block;
        this.location = location;
    }

    public Block getBlock() {
        return block;
    }

    public SeatPose setBlock(Block Block) {
        block = Block;
        return this;
    }

    public Location getLocation() {
        return location.clone();
    }

    public SeatPose setLocation(Location Location) {
        location = Location;
        return this;
    }

    public Entity getSeatEntity() {
        return seatEntity;
    }

    public Player getRider() {
        return rider;
    }

    public double initialYOffset() {
        return initialYOffset;
    }

    public String toString() {
        return seatEntity.getUniqueId().toString();
    }

}