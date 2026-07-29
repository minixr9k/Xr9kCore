package dev.minixr9k.packets.beta.play;

import dev.minixr9k.utils.BetaPacket;
import io.netty.buffer.ByteBuf;

public class PlayerPositionAndLook13Packet implements BetaPacket {

    private double x;
    private double y;
    private double stance;
    private double z;
    private float yaw;
    private float pitch;
    private boolean onGround;

    public PlayerPositionAndLook13Packet() {}

    // Конструктор для отправки с сервера клиенту
    public PlayerPositionAndLook13Packet(double x, double y, double stance, double z, float yaw, float pitch, boolean onGround) {
        this.x = x;
        this.y = y;
        this.stance = stance;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.onGround = onGround;
    }

    @Override
    public void write(ByteBuf out, int protocolVersion) {
        // Server -> Client: X, Stance, Y, Z, Yaw, Pitch, OnGround
        out.writeDouble(x);
        out.writeDouble(stance);
        out.writeDouble(y);
        out.writeDouble(z);
        out.writeFloat(yaw);
        out.writeFloat(pitch);
        out.writeBoolean(onGround);
    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {
        // Client -> Server: X, Y, Stance, Z, Yaw, Pitch, OnGround
        this.x = in.readDouble();
        this.y = in.readDouble();
        this.stance = in.readDouble();
        this.z = in.readDouble();
        this.yaw = in.readFloat();
        this.pitch = in.readFloat();
        this.onGround = in.readBoolean();
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getStance() { return stance; }
    public double getZ() { return z; }
    public float getYaw() { return yaw; }
    public float getPitch() { return pitch; }
    public boolean isOnGround() { return onGround; }

    @Override
    public int getPacketId(int protocolVersion) {
        return 0x0D;
    }
}