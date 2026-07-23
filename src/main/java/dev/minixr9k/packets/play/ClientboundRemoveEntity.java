package dev.minixr9k.packets.play;

import dev.minixr9k.utils.MinecraftPacket;
import io.netty.buffer.ByteBuf;

import static dev.minixr9k.utils.ProtocolUtils.writeVarInt;

public class ClientboundRemoveEntity implements MinecraftPacket {

    private int entityId;

    public ClientboundRemoveEntity(int entityId) {
        this.entityId = entityId;
    }

    @Override
    public void write(ByteBuf out, int protocolVersion) {
        writeVarInt(out, 1);
        writeVarInt(out, entityId);
    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {

    }

    @Override
    public int getPacketId(int protocolVersion) {
        if (protocolVersion == 773)
            return 0x4D;
        else if (protocolVersion > 773)
            return 0x4B;
        return 0x46;
    }
}
