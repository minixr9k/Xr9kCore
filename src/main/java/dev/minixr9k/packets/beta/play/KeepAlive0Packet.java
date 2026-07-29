package dev.minixr9k.packets.beta.play;

import dev.minixr9k.utils.BetaPacket;
import io.netty.buffer.ByteBuf;

public class KeepAlive0Packet implements BetaPacket {

    public KeepAlive0Packet() {}

    @Override
    public void write(ByteBuf out, int protocolVersion) {

    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {

    }

    @Override
    public int getPacketId(int protocolVersion) {
        return 0x00;
    }
}
