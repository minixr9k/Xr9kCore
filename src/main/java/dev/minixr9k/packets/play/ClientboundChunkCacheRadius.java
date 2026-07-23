package dev.minixr9k.packets.play;

import dev.minixr9k.utils.MinecraftPacket;
import io.netty.buffer.ByteBuf;

import static dev.minixr9k.utils.ProtocolUtils.writeVarInt;

public class ClientboundChunkCacheRadius implements MinecraftPacket {

    private final int viewDistance;

    public ClientboundChunkCacheRadius(int viewDistance) {
        this.viewDistance = viewDistance;
    }

    @Override
    public void write(ByteBuf out, int protocolVersion) {
        writeVarInt(out, viewDistance);
    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {

    }

    @Override
    public int getPacketId(int protocolVersion) {
        return 0x58;
    }
}
