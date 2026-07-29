package dev.minixr9k.api.event;

import dev.minixr9k.api.Event;
import dev.minixr9k.types.Player;

public class PlayerChatEvent extends Event {
    private final Player player;
    private final String message;
    private String style;

    public PlayerChatEvent(Player player, String message) {
        this.player = player;
        this.message = message;
        this.style = String.format("<%s> %s", player.getUsername(), message);
    }

    public Player getPlayer() {
        return player;
    }

    public String getMessage() {
        return message;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }
}
