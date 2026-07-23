package dev.minixr9k.packets.play;

import dev.minixr9k.utils.MinecraftPacket;
import io.netty.buffer.ByteBuf;

import static dev.minixr9k.utils.ProtocolUtils.writeVarInt;

public class ClientboundMoveEntityRot implements MinecraftPacket {

    private int entityId;
    private byte yaw;
    private byte pitch;
    private boolean onGround;

    public ClientboundMoveEntityRot(int entityId, byte yaw, byte pitch, boolean onGround) {
        this.entityId = entityId;
        this.yaw = yaw;
        this.pitch = pitch;
        this.onGround = onGround;
    }

    @Override
    public void write(ByteBuf out, int protocolVersion) {
        writeVarInt(out, entityId);
        out.writeByte(yaw);
        out.writeByte(pitch);
        out.writeBoolean(onGround);
    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {

    }

    @Override
    public int getPacketId(int protocolVersion) {
        return 0x31;
    }
}
