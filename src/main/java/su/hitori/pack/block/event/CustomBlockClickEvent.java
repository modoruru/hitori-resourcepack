package su.hitori.pack.block.event;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import su.hitori.pack.type.block.CustomBlock;

public final class CustomBlockClickEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Player player;
    private final CustomBlock customBlock;
    private final Block clicked;

    private boolean consumeInput;

    public CustomBlockClickEvent(Player player, CustomBlock customBlock, Block clicked) {
        this.player = player;
        this.customBlock = customBlock;
        this.clicked = clicked;
    }

    public Player player() {
        return player;
    }

    public CustomBlock customBlock() {
        return customBlock;
    }

    public Block clicked() {
        return clicked;
    }

    public void consumeInput(boolean consumeInput) {
        this.consumeInput = consumeInput;
    }

    public boolean consumeInput() {
        return consumeInput;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

}
