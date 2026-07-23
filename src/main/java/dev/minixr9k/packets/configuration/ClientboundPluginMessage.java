package dev.minixr9k.packets.configuration;

import dev.minixr9k.utils.MinecraftPacket;
import io.netty.buffer.ByteBuf;

import static dev.minixr9k.utils.ProtocolUtils.writeString;

public class ClientboundPluginMessage implements MinecraftPacket {

    private final String channel;
    private final String message;

    public ClientboundPluginMessage(String channel, String message) {
        this.channel = channel;
        this.message = message;
    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {}

    @Override
    public void write(ByteBuf out, int protocolVersion) {
        writeString(out, channel);
        writeString(out, message);
    }

    @Override
    public int getPacketId(int protocolVersion) {
        return 0x01;
    }
}
