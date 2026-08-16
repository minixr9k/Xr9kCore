package dev.minixr9k.api.event;

import dev.minixr9k.api.Cancellable;
import dev.minixr9k.api.Event;
import dev.minixr9k.types.Player;

public class InventoryClickEvent extends Event implements Cancellable {

    private final Player player;
    private final int windowId;
    private final int stateId;
    private final short slot;
    private final byte button;
    private final int mode;

    private boolean cancelled;

    public InventoryClickEvent(Player player, int windowId, int stateId, short slot, byte button, int mode) {
        this.player = player;
        this.windowId = windowId;
        this.stateId = stateId;
        this.slot = slot;
        this.button = button;
        this.mode = mode;
    }

    public Player getPlayer() {
        return player;
    }

    public int getWindowId() {
        return windowId;
    }

    public int getStateId() {
        return stateId;
    }

    public short getSlot() {
        return slot;
    }

    public byte getButton() {
        return button;
    }

    public int getMode() {
        return mode;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }
}
