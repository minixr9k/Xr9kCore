package dev.minixr9k.api.event;

import dev.minixr9k.api.Event;
import dev.minixr9k.types.Block;
import dev.minixr9k.types.Player;

public class BlockBreakEvent extends Event {

    private final Player player;
    private final Block block;

    public BlockBreakEvent(Player player, Block block) {
        this.player = player;
        this.block = block;
    }

    public Player getPlayer() {
        return player;
    }

    public Block getBlock() {
        return block;
    }
}
