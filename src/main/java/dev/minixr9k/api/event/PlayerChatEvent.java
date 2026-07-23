package dev.minixr9k.api.event;

import dev.minixr9k.api.Event;
import dev.minixr9k.types.Player;

public class PlayerChatEvent extends Event {
    private final Player player;
    private final String message;

    public PlayerChatEvent(Player player, String message) {
        this.player = player;
        this.message = message;
    }

    public Player getPlayer() {
        return player;
    }

    public String getMessage() {
        return message;
    }
}
