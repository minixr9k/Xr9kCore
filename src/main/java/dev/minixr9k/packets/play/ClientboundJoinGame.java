package dev.minixr9k.packets.play;

import dev.minixr9k.config.Configuration;
import dev.minixr9k.types.GameMode;
import dev.minixr9k.types.Player;
import dev.minixr9k.utils.MinecraftPacket;
import io.netty.buffer.ByteBuf;

import static dev.minixr9k.utils.ProtocolUtils.writeString;
import static dev.minixr9k.utils.ProtocolUtils.writeVarInt;

public class ClientboundJoinGame implements MinecraftPacket {

    private final GameMode gameMode;

    public ClientboundJoinGame(GameMode gameMode, Player player) {
        this.gameMode = gameMode;
        player.setSystemGameMode(gameMode);
    }

    @Override
    public void write(ByteBuf out, int protocolVersion) {
        out.writeInt(1);         // Entity ID
        out.writeBoolean(Configuration.get().hardcore); // Hardcore

        // Список миров
        writeVarInt(out, 1);
        writeString(out, "minecraft:overworld");

        writeVarInt(out, 10);    // Max Players (unused)
        writeVarInt(out, Configuration.get().world.renderDistance);     // View Distance
        writeVarInt(out, Configuration.get().world.simulationDistance);     // Simulation Distance
        out.writeBoolean(false); // Reduced Debug Info
        out.writeBoolean(true);  // Enable Respawn Screen
        out.writeBoolean(false); // Do Limited Crafting

        writeVarInt(out, 0);
        writeString(out, "minecraft:overworld");

        out.writeLong(0L);       // Hashed Seed
        out.writeByte(gameMode.getId());        // Gamemode (1 - Creative)
        out.writeByte(-1);       // Previous Gamemode
        out.writeBoolean(false); // Is Debug
        out.writeBoolean(Configuration.get().world.flatType); // Is Flat
        out.writeBoolean(false); // Has Death Location
        writeVarInt(out, 0);     // Portal Cooldown
        if (protocolVersion > 767)
            writeVarInt(out, 63);    // Sea Level
        out.writeBoolean(true); // Enforces Secure Chat
    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {

    }

    @Override
    public int getPacketId(int protocolVersion) {
        if (protocolVersion >= 773)
            return 0x30;
        else if (protocolVersion >= 770)
            return 0x2B;
        else if (protocolVersion >= 768)
            return 0x2C;
        else
            return 0x2B;
    }
}
