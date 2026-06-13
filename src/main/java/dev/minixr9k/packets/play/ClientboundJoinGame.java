package dev.minixr9k.packets.play;

import dev.minixr9k.utils.MinecraftPacket;
import io.netty.buffer.ByteBuf;

import static dev.minixr9k.utils.ProtocolUtils.writeString;
import static dev.minixr9k.utils.ProtocolUtils.writeVarInt;

public class ClientboundJoinGame implements MinecraftPacket {
    @Override
    public void write(ByteBuf out, int protocolVersion) {
        out.writeInt(1);         // Entity ID
        out.writeBoolean(true); // Hardcore

        // Список миров
        writeVarInt(out, 1);
        writeString(out, "minecraft:overworld");

        writeVarInt(out, 10);    // Max Players
        writeVarInt(out, 8);     // View Distance
        writeVarInt(out, 8);     // Simulation Distance
        out.writeBoolean(false); // Reduced Debug Info
        out.writeBoolean(true);  // Enable Respawn Screen
        out.writeBoolean(false); // Do Limited Crafting

        writeVarInt(out, 0);
        writeString(out, "minecraft:overworld");

        out.writeLong(0L);       // Hashed Seed
        out.writeByte(2);        // Gamemode (1 - Creative)
        out.writeByte(-1);       // Previous Gamemode
        out.writeBoolean(false); // Is Debug
        out.writeBoolean(false); // Is Flat
        out.writeBoolean(false); // Has Death Location
        writeVarInt(out, 0);     // Portal Cooldown
        if (protocolVersion > 767)
            writeVarInt(out, 63);    // Sea Level
        out.writeBoolean(false); // Enforces Secure Chat
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
