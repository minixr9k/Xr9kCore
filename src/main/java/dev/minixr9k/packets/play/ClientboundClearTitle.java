package dev.minixr9k.packets.play;

import dev.minixr9k.utils.MinecraftPacket;
import io.netty.buffer.ByteBuf;

public class ClientboundClearTitle implements MinecraftPacket {

    private final boolean reset;

    public ClientboundClearTitle(boolean reset) {
        this.reset = reset;
    }

    @Override
    public void write(ByteBuf out, int protocolVersion) {
        out.writeBoolean(reset);
    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {

    }

    @Override
    public int getPacketId(int protocolVersion) {
        return 0x0E;
    }
}
