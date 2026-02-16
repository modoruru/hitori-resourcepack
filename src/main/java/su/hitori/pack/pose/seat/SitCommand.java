package su.hitori.pack.pose.seat;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.executors.CommandArguments;
import org.bukkit.Location;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import su.hitori.pack.PackModule;

public final class SitCommand extends CommandAPICommand {

    private final PackModule packModule;

    public SitCommand(PackModule packModule) {
        super("sit");
        this.packModule = packModule;

        executesPlayer(this::execute);
    }

    private void execute(Player player, CommandArguments ignored) {
        SeatPose seatPose = packModule.poseService().getSeatPoseByRider(player);
        if(seatPose != null) {
            packModule.poseService().removeSeatPose(seatPose);
            return;
        }

        if(!player.isValid() || player.isSneaking() || !player.isOnGround() || player.getVehicle() != null || player.isSleeping() || packModule.poseService().isBlocked(player)) {
            return;
        }

        Location playerLocation = player.getLocation();
        Block block = playerLocation.getBlock().isPassable()
                ? playerLocation.subtract(0, 0.0625, 0).getBlock()
                : playerLocation.getBlock();

        boolean overSize = false;
        try {
            for(BoundingBox boundingBox : block.getCollisionShape().getBoundingBoxes()) {
                if (boundingBox.getMaxY() > 1.25) {
                    overSize = true;
                    break;
                }
            }
        }
        catch(Throwable _) {
        }

        if(!(block.getRelative(BlockFace.UP).isPassable() && !overSize && !block.isPassable())) {
            return;
        }

        if(packModule.poseService().isBlockOccupied(block)) return;

        if(Tag.STAIRS.isTagged(block.getType())) packModule.poseService().createStairSeatForEntity(block, player);
        else packModule.poseService().createSeatPose(block, player);
    }

}
