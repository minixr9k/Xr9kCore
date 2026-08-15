package dev.minixr9k.packets.play;

import dev.minixr9k.utils.MinecraftPacket;
import io.netty.buffer.ByteBuf;

import java.util.List;

import static dev.minixr9k.utils.ProtocolUtils.writeString;
import static dev.minixr9k.utils.ProtocolUtils.writeVarInt;

public class ClientboundCommands implements MinecraftPacket {

    private final List<String> commands;

    public ClientboundCommands(List<String> commands) {
        this.commands = commands;
    }

    @Override
    public void write(ByteBuf out, int protocolVersion) {
        writeVarInt(out, 1 + commands.size());

        out.writeByte(0); // root
        writeVarInt(out, commands.size()); // children count
        for (int i = 0; i < commands.size(); i++) {
            writeVarInt(out, i + 1);
        }

        for (String command : commands) {
            out.writeByte(1); // literal
            writeVarInt(out, 0); // children count
            writeString(out, command);
        }

        writeVarInt(out, 0);
    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {

    }

    @Override
    public int getPacketId(int protocolVersion) {
        return 0x10;
    }
}
