package dev.minixr9k.packets.play;

import dev.minixr9k.utils.MinecraftPacket;
import io.netty.buffer.ByteBuf;

import static dev.minixr9k.utils.ProtocolUtils.writeVarInt;

public class ClientboundSetPassengers implements MinecraftPacket {

    private final int entityId;
    private final int passengerId;

    public ClientboundSetPassengers(int entityId, int passengerId) {
        this.entityId = entityId;
        this.passengerId = passengerId;
    }

    @Override
    public void write(ByteBuf out, int protocolVersion) {
        writeVarInt(out, entityId);
        writeVarInt(out, 1);
        writeVarInt(out, passengerId);
    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {

    }

    @Override
    public int getPacketId(int protocolVersion) {
        if (protocolVersion > 772)
            return 0x69;
        return 0x64;
    }
}
