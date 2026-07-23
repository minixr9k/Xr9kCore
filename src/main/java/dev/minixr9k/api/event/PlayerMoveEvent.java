package dev.minixr9k.api.event;

import dev.minixr9k.api.Event;
import dev.minixr9k.types.Player;

public class PlayerMoveEvent extends Event {

    private final Player player;
    private final double toX;
    private final double toY;
    private final double toZ;
    private final float toYaw;
    private final float toPitch;

    public PlayerMoveEvent(Player player, double toX, double toY, double toZ, float toYaw, float toPitch) {
        this.player = player;
        this.toX = toX;
        this.toY = toY;
        this.toZ = toZ;
        this.toYaw = toYaw;
        this.toPitch = toPitch;
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

}
