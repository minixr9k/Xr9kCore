package dev.minixr9k.packets.beta.play;

import dev.minixr9k.utils.BetaPacket;
import io.netty.buffer.ByteBuf;

public class EntityTeleport34Packet implements BetaPacket {

    private int entityId;
    private int x;
    private int y;
    private int z;
    private byte yaw;
    private byte pitch;

    public EntityTeleport34Packet() {}

    public EntityTeleport34Packet(int entityId, double x, double y, double z, float yaw, float pitch) {
        this.entityId = entityId;
        this.x = (int)(x * 32.0F);
        this.y = (int)(y * 32.0F);
        this.z = (int)(z * 32.0F);
        this.yaw = (byte) (yaw * 256.0F / 360.0F);
        this.pitch = (byte) (pitch * 256.0F / 360.0F);
    }

    @Override
    public void write(ByteBuf out, int protocolVersion) {
        out.writeInt(entityId);
        out.writeInt(x);
        out.writeInt(y);
        out.writeInt(z);
        out.writeByte(yaw);
        out.writeByte(pitch);
    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {
        this.entityId = in.readInt();
        this.x = in.readInt();
        this.y = in.readInt();
        this.z = in.readInt();
        this.yaw = in.readByte();
        this.pitch = in.readByte();
    }

    public int getEntityId() { return entityId; }
    public double getRealX() { return x / 32.0D; }
    public double getRealY() { return y / 32.0D; }
    public double getRealZ() { return z / 32.0D; }

    @Override
    public int getPacketId(int protocolVersion) {
        return 0x22;
    }
}