package dev.minixr9k.packets.play;

import dev.minixr9k.utils.MinecraftPacket;
import io.netty.buffer.ByteBuf;

import java.util.List;

import static dev.minixr9k.utils.ProtocolUtils.writeVarInt;

public class ClientboundSetPassengersList implements MinecraftPacket {

    private final int entityId;
    private final List<Integer> passengers;

    public ClientboundSetPassengersList(int entityId, List<Integer> passengers) {
        this.entityId = entityId;
        this.passengers = passengers;
    }

    @Override
    public void write(ByteBuf out, int protocolVersion) {
        writeVarInt(out, entityId);
        writeVarInt(out, passengers.size());
        for (int passengerId : passengers)
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
