package dev.minixr9k.api.event;

import dev.minixr9k.api.Event;
import dev.minixr9k.types.Player;

public class PlayerCommandEvent extends Event {

    private final Player player;
    private final String command;

    public PlayerCommandEvent(Player player, String command) {
        this.player = player;
        this.command = command;
    }

    public Player getPlayer() {
        return player;
    }

    public String getCommand() {
        return command;
    }

}
