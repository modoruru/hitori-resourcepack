package su.hitori.pack.pose.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

public final class PlayerStopPoseEvent extends PlayerPoseEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    public PlayerStopPoseEvent(Player player, PoseType poseType) {
        super(player, poseType);
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

}
