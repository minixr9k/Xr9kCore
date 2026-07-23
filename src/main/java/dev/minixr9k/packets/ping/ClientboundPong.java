package dev.minixr9k.packets.ping;

import dev.minixr9k.utils.MinecraftPacket;
import io.netty.buffer.ByteBuf;

public class ClientboundPong implements MinecraftPacket {

    @Override
    public void read(ByteBuf in, int protocolVersion) {}

    @Override
    public void write(ByteBuf out, int protocolVersion) {
        out.writeLong(System.currentTimeMillis());
    }

    @Override
    public int getPacketId(int protocolVersion) {
        return 0x01;
    }
}
