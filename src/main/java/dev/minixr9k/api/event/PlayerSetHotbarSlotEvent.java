package dev.minixr9k.api.event;

import dev.minixr9k.api.Event;
import dev.minixr9k.types.Player;

public class PlayerSetHotbarSlotEvent extends Event {

    private final Player player;
    private final short slot;

    public PlayerSetHotbarSlotEvent(Player player, short slot) {
        this.player = player;
        this.slot = slot;
    }

    public Player getPlayer() { return player; }
    public short getSlot() { return slot; }

}
