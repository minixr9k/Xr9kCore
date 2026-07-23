package dev.minixr9k.packets.play;

import dev.minixr9k.utils.MinecraftPacket;
import io.netty.buffer.ByteBuf;

import static dev.minixr9k.utils.ProtocolUtils.writeVarInt;

public class ClientboundMoveEntityPos implements MinecraftPacket {

    private int entityId;
    private short deltaX;
    private short deltaY;
    private short deltaZ;
    private boolean onGround;

    public ClientboundMoveEntityPos(int entityId, short deltaX, short deltaY, short deltaZ, boolean onGround) {
        this.entityId = entityId;
        this.deltaX = deltaX;
        this.deltaY = deltaY;
        this.deltaZ = deltaZ;
        this.onGround = onGround;
    }

    @Override
    public void write(ByteBuf out, int protocolVersion) {
        writeVarInt(out, entityId);
        out.writeShort(deltaX);
        out.writeShort(deltaY);
        out.writeShort(deltaZ);
        out.writeBoolean(onGround);
    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {

    }

    @Override
    public int getPacketId(int protocolVersion) {
        return 0x2E;
    }
}
