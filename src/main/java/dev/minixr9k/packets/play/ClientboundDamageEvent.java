package dev.minixr9k.packets.play;

import dev.minixr9k.utils.MinecraftPacket;
import io.netty.buffer.ByteBuf;

import static dev.minixr9k.utils.ProtocolUtils.writeVarInt;

public class ClientboundDamageEvent implements MinecraftPacket {

    private int entityId;
    private int damageType;

    public ClientboundDamageEvent(int entityId, int damageType) {
        this.entityId = entityId;
        this.damageType = damageType;
    }

    @Override
    public void write(ByteBuf out, int protocolVersion) {
        writeVarInt(out, entityId);
        writeVarInt(out, damageType);
        writeVarInt(out, 0);
        writeVarInt(out, 0);
        out.writeBoolean(false);
    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {

    }

    @Override
    public int getPacketId(int protocolVersion) {
        return 0x19;
    }
}
