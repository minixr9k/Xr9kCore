package dev.minixr9k.packets.login.serverbound;

import dev.minixr9k.utils.MinecraftPacket;
import io.netty.buffer.ByteBuf;

public class ServerboundLoginAck implements MinecraftPacket {
    @Override
    public void write(ByteBuf out, int protocolVersion) {

    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {

    }

    @Override
    public int getPacketId(int protocolVersion) {
        return 0x03;
    }
}
