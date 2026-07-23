package dev.minixr9k.api.event;

import dev.minixr9k.api.Event;
import dev.minixr9k.types.Player;

public class PlayerJoinEvent extends Event {
    private final Player player;

    public PlayerJoinEvent(Player player) {
        this.player = player;
    }

    public Player getPlayer() { return player; }
}
