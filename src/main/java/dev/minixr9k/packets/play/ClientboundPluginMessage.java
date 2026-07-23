package dev.minixr9k.packets.play;

import dev.minixr9k.utils.MinecraftPacket;
import io.netty.buffer.ByteBuf;

import static dev.minixr9k.utils.ProtocolUtils.*;

public class ClientboundPluginMessage implements MinecraftPacket {

    private final String channel;
    private final String command;
    private final String value;

    public ClientboundPluginMessage(String channel, String command, String value) {
        this.channel = channel;
        this.command = command;
        this.value = value;
    }

    @Override
    public void write(ByteBuf out, int protocolVersion) {
        writeString(out, channel);
        writeStringWithShort(out, command);
        writeStringWithShort(out, value);
    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {

    }

    @Override
    public int getPacketId(int protocolVersion) {
        if (protocolVersion >= 770)
            return 0x18;
        else
            return 0x19;
    }
}
