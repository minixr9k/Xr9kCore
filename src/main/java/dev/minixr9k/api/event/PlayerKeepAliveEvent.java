package dev.minixr9k.api.event;

import dev.minixr9k.api.Event;
import dev.minixr9k.types.Player;

public class PlayerKeepAliveEvent extends Event {
    private final Player player;

    public PlayerKeepAliveEvent(Player player) {
        this.player = player;
    }

    public Player getPlayer() { return player; }

}
