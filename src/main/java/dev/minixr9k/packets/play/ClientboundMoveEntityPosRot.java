package dev.minixr9k.packets.play;

import dev.minixr9k.utils.MinecraftPacket;
import io.netty.buffer.ByteBuf;

import static dev.minixr9k.utils.ProtocolUtils.writeVarInt;

public class ClientboundMoveEntityPosRot implements MinecraftPacket {

    private int entityId;
    private short deltaX;
    private short deltaY;
    private short deltaZ;
    private byte yaw;
    private byte pitch;
    private boolean onGround;

    public ClientboundMoveEntityPosRot(int entityId, short deltaX, short deltaY, short deltaZ, byte yaw, byte pitch, boolean onGround) {
        this.entityId = entityId;
        this.deltaX = deltaX;
        this.deltaY = deltaY;
        this.deltaZ = deltaZ;
        this.yaw = yaw;
        this.pitch = pitch;
        this.onGround = onGround;
    }

    @Override
    public void write(ByteBuf out, int protocolVersion) {
        writeVarInt(out, entityId);
        out.writeShort(deltaX);
        out.writeShort(deltaY);
        out.writeShort(deltaZ);
        out.writeByte(yaw);
        out.writeByte(pitch);
        out.writeBoolean(onGround);
    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {

    }

    @Override
    public int getPacketId(int protocolVersion) {
        return 0x2F;
    }
}
