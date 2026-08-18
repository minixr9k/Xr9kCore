package dev.minixr9k.api.event;

import dev.minixr9k.api.Event;
import dev.minixr9k.types.Player;

public class CustomActionEvent extends Event {

    private final Player player;
    private final String action;

    public CustomActionEvent(Player player, String action) {
        this.player = player;
        this.action = action;
    }

    public Player getPlayer() { return player; }

    public String getPacketMessage() {
        return action;
    }
}
