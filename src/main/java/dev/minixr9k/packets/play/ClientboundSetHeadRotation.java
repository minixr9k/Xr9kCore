package dev.minixr9k.packets.play;

import dev.minixr9k.utils.MinecraftPacket;
import io.netty.buffer.ByteBuf;

import static dev.minixr9k.utils.ProtocolUtils.writeVarInt;

public class ClientboundSetHeadRotation implements MinecraftPacket {

    private int entityId;
    private byte angle;

    public ClientboundSetHeadRotation(int entityId, byte angle) {
        this.entityId = entityId;
        this.angle = angle;
    }

    @Override
    public void write(ByteBuf out, int protocolVersion) {
        writeVarInt(out, entityId);
        out.writeByte(angle);
    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {

    }

    @Override
    public int getPacketId(int protocolVersion) {
        if (protocolVersion > 772)
            return 0x51;
        return 0x4C;
    }
}
