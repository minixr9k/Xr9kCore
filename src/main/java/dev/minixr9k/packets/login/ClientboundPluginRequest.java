package dev.minixr9k.packets.login;

import dev.minixr9k.utils.MinecraftPacket;
import io.netty.buffer.ByteBuf;

import static dev.minixr9k.utils.ProtocolUtils.*;

public class ClientboundPluginRequest implements MinecraftPacket {

    private final int messageId;
    private final String channel;
    private final ByteBuf data;

    public ClientboundPluginRequest(int messageId, String channel, ByteBuf data) {
        this.messageId = messageId;
        this.channel = channel;
        this.data = data;
    }

    @Override
    public void write(ByteBuf out, int protocolVersion) {
        writeVarInt(out, messageId);
        writeString(out, channel);
        if (data != null && data.isReadable()) {
            out.writeBytes(data);
        }
    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {

    }

    @Override
    public int getPacketId(int protocolVersion) {
        return 0x04;
    }
}
