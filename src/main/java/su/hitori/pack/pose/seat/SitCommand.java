package su.hitori.pack.pose.seat;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Location;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import su.hitori.pack.PackModule;

public final class SitCommand {

    private SitCommand() {}

    public static LiteralCommandNode<CommandSourceStack> bootstrap(PackModule packModule) {
        return Commands.literal("sit")
                .requires(source -> source.getSender() instanceof Player)
                .executes(context -> {
                    Player player = (Player) context.getSource().getSender();

                    SeatPose seatPose = packModule.poseService().getSeatPoseByRider(player);
                    if(seatPose != null) {
                        packModule.poseService().removeSeatPose(seatPose);
                        return 0;
                    }

                    if(!player.isValid() || player.isSneaking() || !player.isOnGround() || player.getVehicle() != null || player.isSleeping() || packModule.poseService().isBlocked(player)) {
                        return 0;
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
                        return 0 ;
                    }

                    if(packModule.poseService().isBlockOccupied(block)) return 0;

                    if(Tag.STAIRS.isTagged(block.getType())) packModule.poseService().createStairSeatForEntity(block, player);
                    else packModule.poseService().createSeatPose(block, player);

                    return 1;
                })
                .build();
    }

}
