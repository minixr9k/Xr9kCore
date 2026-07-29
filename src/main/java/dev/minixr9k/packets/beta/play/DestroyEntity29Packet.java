package dev.minixr9k.packets.beta.play;

import dev.minixr9k.utils.BetaPacket;
import io.netty.buffer.ByteBuf;

public class DestroyEntity29Packet implements BetaPacket {
    private int entityId;

    public DestroyEntity29Packet() {}

    public DestroyEntity29Packet(int entityId) {
        this.entityId = entityId;
    }

    @Override
    public void write(ByteBuf out, int protocolVersion) {
        out.writeInt(entityId);
    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {

    }

    @Override
    public int getPacketId(int protocolVersion) {
        return 0x1D;
    }
}
