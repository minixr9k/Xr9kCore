package dev.minixr9k.packets.confAndPlay.serverbound;

import dev.minixr9k.utils.MinecraftPacket;
import io.netty.buffer.ByteBuf;

import static dev.minixr9k.utils.ProtocolUtils.readString;

public class ServerboundPluginMessage implements MinecraftPacket {

    private String channel;
    private String data;

    @Override
    public void write(ByteBuf out, int protocolVersion) {

    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {
        this.channel = readString(in);
        this.data = readString(in);
    }

    public String getChannel() {
        return channel;
    }

    public String getData() {
        return data;
    }

    @Override
    public int getPacketId(int protocolVersion) {
        if (protocolVersion == -1)
            return 0x02;
        return 0x16;
    }
}
