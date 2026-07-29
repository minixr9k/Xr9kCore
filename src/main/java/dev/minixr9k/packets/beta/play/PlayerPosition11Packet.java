package dev.minixr9k.packets.beta.play;

import dev.minixr9k.utils.BetaPacket;
import io.netty.buffer.ByteBuf;

public class PlayerPosition11Packet implements BetaPacket {

    private double x;
    private double y;
    private double stance;
    private double z;
    private boolean onGround;

    public PlayerPosition11Packet() {}

    @Override
    public void write(ByteBuf out, int protocolVersion) {

    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {
        this.x = in.readDouble();
        this.y = in.readDouble();
        this.stance = in.readDouble();
        this.z = in.readDouble();
        this.onGround = in.readBoolean();
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getStance() { return stance; }
    public double getZ() { return z; }
    public boolean isOnGround() { return onGround; }

    @Override
    public int getPacketId(int protocolVersion) {
        return 0x0B;
    }
}