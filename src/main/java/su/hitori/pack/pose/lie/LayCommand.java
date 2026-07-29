package su.hitori.pack.pose.lie;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import su.hitori.pack.pose.PoseService;

public final class LayCommand {

    private LayCommand() {}

    public static LiteralCommandNode<CommandSourceStack> bootstrap(PoseService poseService) {
        return Commands.literal("lay")
                .requires(source -> source.getSender() instanceof Player)
                .executes(context -> {
                    Player player = (Player) context.getSource().getSender();

                    LyingPose existingPose = poseService.getLyingPoseByRider(player);
                    if(existingPose != null) {
                        poseService.removeLyingPose(existingPose);
                        return 0;
                    }

                    if(!player.isValid() || player.isSneaking() || !player.isOnGround() || player.getVehicle() != null || player.isSleeping() || poseService.isBlocked(player)) {
                        return 0;
                    }

                    Location playerLocation = player.getLocation();
                    Block block = playerLocation.getBlock().isPassable()
                            ? playerLocation.subtract(0, 0.0625, 0).getBlock()
                            : playerLocation.getBlock();

                    if(poseService.isBlockOccupied(block)) return 0;

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
                        return 0;
                    }

                    poseService.createLyingPose(block, player);

                    return 1;
                })
                .build();
    }

}
