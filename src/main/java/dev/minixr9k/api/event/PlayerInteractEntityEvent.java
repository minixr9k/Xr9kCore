package dev.minixr9k.api.event;

import dev.minixr9k.api.Event;
import dev.minixr9k.types.Player;

public class PlayerInteractEntityEvent extends Event {

    private final Player player;
    private final int entityId;
    private final int actionType;
    private final boolean isSneaking;

    public PlayerInteractEntityEvent(Player player, int entityId, int actionType, boolean isSneaking) {
        this.player = player;
        this.entityId = entityId;
        this.actionType = actionType;
        this.isSneaking = isSneaking;
    }

    public Player getPlayer() {
        return player;
    }

    public int getEntityId() {
        return entityId;
    }

    public int getActionType() {
        return actionType;
    }

    public boolean isSneaking() {
        return isSneaking;
    }
}
