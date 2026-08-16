package dev.minixr9k.api.event;

import dev.minixr9k.api.Cancellable;
import dev.minixr9k.api.Event;
import dev.minixr9k.types.Player;
import dev.minixr9k.types.actions.MoveType;

public class PlayerMoveEvent extends Event implements Cancellable {

    private final Player player;
    private final double toX;
    private final double toY;
    private final double toZ;
    private final float toYaw;
    private final float toPitch;
    private final boolean onGround;
    private final MoveType moveType;

    private boolean cancelled;

    public PlayerMoveEvent(Player player, double toX, double toY, double toZ, float toYaw, float toPitch, boolean onGround, MoveType moveType) {
        this.player = player;
        this.toX = toX;
        this.toY = toY;
        this.toZ = toZ;
        this.toYaw = toYaw;
        this.toPitch = toPitch;
        this.onGround = onGround;
        this.moveType = moveType;
    }

    public Player getPlayer() {
        return player;
    }

    public double getToX() {
        return toX;
    }

    public double getToY() {
        return toY;
    }

    public double getToZ() {
        return toZ;
    }

    public float getToYaw() {
        return toYaw;
    }

    public float getToPitch() {
        return toPitch;
    }

    public boolean isOnGround() {
        return onGround;
    }

    public MoveType getMoveType() {
        return moveType;
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
