package dev.minixr9k.api.event;

import dev.minixr9k.api.Event;
import dev.minixr9k.types.InputKey;
import dev.minixr9k.types.Player;

public class PlayerInputEvent extends Event {

    private final Player player;
    private final byte flags;
    private final InputKey customKey;

    public PlayerInputEvent(Player player, byte flags) {
        this.player = player;
        this.flags = flags;
        this.customKey = null;
    }

    public PlayerInputEvent(Player player, InputKey customKey) {
        this.player = player;
        this.flags = 0;
        this.customKey = customKey;
    }

    public Player getPlayer() { return player; }
    public int getFlags() { return flags; }

    public boolean isPressed(InputKey key) {

        if (customKey != null) {
            return customKey == key;
        }

        return switch (key) {
            case KEY_W     -> (flags & 0x01) != 0;
            case KEY_A     -> (flags & 0x04) != 0;
            case KEY_S     -> (flags & 0x02) != 0;
            case KEY_D     -> (flags & 0x08) != 0;
            case KEY_SPACE -> (flags & 0x10) != 0;
            case KEY_SHIFT -> (flags & 0x20) != 0;
            case KEY_CTRL  -> (flags & 0x40) != 0;
            default        -> false;
        };
    }

}
