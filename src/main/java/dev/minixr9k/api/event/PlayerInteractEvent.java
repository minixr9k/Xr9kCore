package dev.minixr9k.api.event;

import dev.minixr9k.api.Event;
import dev.minixr9k.types.ActionType;
import dev.minixr9k.types.Player;

public class PlayerInteractEvent extends Event {

    private final Player player;
    private final ActionType actionType;
    private int x, y, z = 0;

    public PlayerInteractEvent(Player player, ActionType actionType) {
        this.player = player;
        this.actionType = actionType;
    }

    public PlayerInteractEvent(Player player, ActionType actionType, int x, int y, int z) {
        this.player = player;
        this.actionType = actionType;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Player getPlayer() {
        return player;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }
}
