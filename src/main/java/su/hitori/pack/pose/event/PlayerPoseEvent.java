package su.hitori.pack.pose.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;

public sealed abstract class PlayerPoseEvent extends Event permits PlayerStartPoseEvent, PlayerStopPoseEvent {

    private final Player player;
    private final PoseType poseType;

    public PlayerPoseEvent(Player player, PoseType poseType) {
        this.player = player;
        this.poseType = poseType;
    }

    public final Player player() {
        return player;
    }

    public final PoseType poseType() {
        return poseType;
    }

}
