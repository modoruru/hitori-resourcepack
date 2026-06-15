package su.hitori.pack.block.event;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Nullable;
import su.hitori.pack.type.block.CustomBlock;

import java.util.Collection;
import java.util.List;

public final class CustomBlockPlaceEvent extends Event implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final CustomBlock customBlock;
    private final List<Block> changedBlocks;

    @Nullable
    private final Player player;

    private boolean cancelled;

    public CustomBlockPlaceEvent(CustomBlock customBlock, Collection<Block> changedBlocks, @Nullable Player player) {
        this.customBlock = customBlock;
        this.changedBlocks = List.copyOf(changedBlocks);
        this.player = player;
    }

    public CustomBlock customBlock() {
        return customBlock;
    }

    public List<Block> changedBlocks() {
        return List.copyOf(changedBlocks);
    }

    public @Nullable Player player() {
        return player;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return getHandlerList();
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

}
