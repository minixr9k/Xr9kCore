package dev.minixr9k.packets.configuration;

import dev.minixr9k.utils.MinecraftPacket;
import io.netty.buffer.ByteBuf;

public class ClientboundFinishConfiguration implements MinecraftPacket {

    @Override
    public void read(ByteBuf in, int protocolVersion) {}

    @Override
    public void write(ByteBuf out, int protocolVersion) {

    }

    @Override
    public int getPacketId(int protocolVersion) {
        return 0x03;
    }
}
