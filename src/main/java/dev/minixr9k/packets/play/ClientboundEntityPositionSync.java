package dev.minixr9k.packets.play;

import dev.minixr9k.utils.MinecraftPacket;
import io.netty.buffer.ByteBuf;

import static dev.minixr9k.utils.ProtocolUtils.writeVarInt;

public class ClientboundEntityPositionSync implements MinecraftPacket {

    private final int entityId;
    private final double x;
    private final double y;
    private final double z;
    private final double velocityX;
    private final double velocityY;
    private final double velocityZ;
    private final float yaw;
    private final float pitch;
    private final boolean onGround;

    public ClientboundEntityPositionSync(int entityId, double x, double y, double z, double velocityX, double velocityY, double velocityZ, float yaw, float pitch, boolean onGround) {
        this.entityId = entityId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.velocityZ = velocityZ;
        this.yaw = yaw;
        this.pitch = pitch;
        this.onGround = onGround;
    }

    @Override
    public void write(ByteBuf out, int protocolVersion) {
        writeVarInt(out, entityId);
        out.writeDouble(x);
        out.writeDouble(y);
        out.writeDouble(z);
        out.writeDouble(velocityX);
        out.writeDouble(velocityY);
        out.writeDouble(velocityZ);
        out.writeFloat(yaw);
        out.writeFloat(pitch);
        out.writeBoolean(onGround);
    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {

    }

    @Override
    public int getPacketId(int protocolVersion) {
        if (protocolVersion == 773)
            return 0x7D;
        return 0x1F;
    }
}
