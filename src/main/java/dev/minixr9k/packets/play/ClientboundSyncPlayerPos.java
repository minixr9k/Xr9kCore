package dev.minixr9k.packets.play;

import dev.minixr9k.utils.MinecraftPacket;
import io.netty.buffer.ByteBuf;

import static dev.minixr9k.utils.ProtocolUtils.writeVarInt;

public class ClientboundSyncPlayerPos implements MinecraftPacket {

    private final int teleportId;
    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final float pitch;

    public ClientboundSyncPlayerPos(int teleportId, double x, double y, double z, float yaw, float pitch) {
        this.teleportId = teleportId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    @Override
    public void write(ByteBuf out, int protocolVersion) {
        if (protocolVersion > 767)
            writeVarInt(out, teleportId);
        out.writeDouble(x);
        out.writeDouble(y);
        out.writeDouble(z);
        if (protocolVersion > 767) {
            out.writeDouble(0);
            out.writeDouble(0);
            out.writeDouble(0);
        }
        out.writeFloat(yaw);
        out.writeFloat(pitch);

        if (protocolVersion <= 767) {
            out.writeByte(0x00);
            writeVarInt(out, teleportId);
        }

        if (protocolVersion > 767)
            out.writeInt(0);
    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {

    }

    @Override
    public int getPacketId(int protocolVersion) {
        if (protocolVersion >= 773)
            return 0x46;
        else if (protocolVersion >= 770)
            return 0x41;
        else if (protocolVersion >= 768)
            return 0x42;
        else
            return 0x40;
    }
}
