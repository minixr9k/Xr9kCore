package dev.minixr9k.packets.play;

import dev.minixr9k.utils.MinecraftPacket;
import dev.minixr9k.utils.ProtocolUtils;
import io.netty.buffer.ByteBuf;
import java.util.UUID;

import static dev.minixr9k.utils.ProtocolUtils.writeVarInt;

public class ClientboundSpawnEntity implements MinecraftPacket {

    private final int entityId;
    private final UUID entityUuid;
    private final int type;
    private final double x;
    private final double y;
    private final double z;
    private final float pitch;
    private final float yaw;
    private final float headYaw;
    private final int data;
    private final short velocityX;
    private final short velocityY;
    private final short velocityZ;

    public ClientboundSpawnEntity(int entityId, UUID entityUuid, int type,
                                  double x, double y, double z,
                                  float yaw, float pitch, float headYaw,
                                  int data, double vx, double vy, double vz) {
        this.entityId = entityId;
        this.entityUuid = entityUuid;
        this.type = type;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.headYaw = headYaw;
        this.data = data;
        this.velocityX = (short) (vx * 8000);
        this.velocityY = (short) (vy * 8000);
        this.velocityZ = (short) (vz * 8000);
    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {}

    @Override
    public void write(ByteBuf out, int protocolVersion) {
        writeVarInt(out, entityId);
        ProtocolUtils.writeUUID(out, entityUuid);
        writeVarInt(out, type);

        out.writeDouble(x);
        out.writeDouble(y);
        out.writeDouble(z);

        if (protocolVersion >= 773)
            out.writeByte(0);

        out.writeByte((byte) (pitch * 256.0F / 360.0F));
        out.writeByte((byte) (yaw * 256.0F / 360.0F));
        out.writeByte((byte) (headYaw * 256.0F / 360.0F));

        if (protocolVersion <= 772) {
            out.writeByte(0);
            out.writeShort(velocityX);
            out.writeShort(velocityY);
            out.writeShort(velocityZ);
        }

        if (protocolVersion >= 773)
            writeVarInt(out, data);
    }

    @Override
    public int getPacketId(int protocolVersion) {
        return 0x01; // Spawn Entity ID для Play состояния
    }
}