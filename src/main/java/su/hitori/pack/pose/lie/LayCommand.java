package su.hitori.pack.pose.lie;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.executors.CommandArguments;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import su.hitori.pack.pose.PoseService;

public final class LayCommand extends CommandAPICommand {

    private final PoseService poseService;

    public LayCommand(PoseService poseService) {
        super("lay");
        this.poseService = poseService;

        executesPlayer(this::execute);
    }

    private void execute(Player player, CommandArguments args) {
        LyingPose existingPose = poseService.getLyingPoseByRider(player);
        if(existingPose != null) {
            poseService.removeLyingPose(existingPose);
            return;
        }

        if(!player.isValid() || player.isSneaking() || !player.isOnGround() || player.getVehicle() != null || player.isSleeping() || poseService.isBlocked(player)) {
            return;
        }

        Location playerLocation = player.getLocation();
        Block block = playerLocation.getBlock().isPassable()
                ? playerLocation.subtract(0, 0.0625, 0).getBlock()
                : playerLocation.getBlock();

        Block supportingBlock = block.getRelative(0, -1, 0);

        if(poseService.isBlockOccupied(block)) return;

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

        poseService.createLyingPose(block, player);
    }

}
