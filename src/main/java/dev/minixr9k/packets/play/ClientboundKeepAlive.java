package dev.minixr9k.packets.play;

import dev.minixr9k.utils.MinecraftPacket;
import io.netty.buffer.ByteBuf;

public class ClientboundKeepAlive implements MinecraftPacket {
    @Override
    public void write(ByteBuf out, int protocolVersion) {
        out.writeLong(System.currentTimeMillis());
    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {

    }

    @Override
    public int getPacketId(int protocolVersion) {
        if (protocolVersion >= 773)
            return 0x2B;
        else if (protocolVersion >= 770)
            return 0x26;
        else if (protocolVersion >= 768)
            return 0x27;
        else
            return 0x26;
    }
}
