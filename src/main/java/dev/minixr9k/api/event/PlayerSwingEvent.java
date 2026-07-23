package dev.minixr9k.api.event;

import dev.minixr9k.api.Event;
import dev.minixr9k.types.Player;

public class PlayerSwingEvent extends Event {

    private final Player player;
    private final int hand;

    public PlayerSwingEvent(Player player, int hand) {
        this.player = player;
        this.hand = hand;
    }

    public Player getPlayer() { return player; }
    public int getHand() { return hand; }

}
