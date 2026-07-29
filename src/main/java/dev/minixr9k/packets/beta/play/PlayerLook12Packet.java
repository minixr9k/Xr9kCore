package dev.minixr9k.packets.beta.play;

import dev.minixr9k.utils.BetaPacket;
import io.netty.buffer.ByteBuf;

public class PlayerLook12Packet implements BetaPacket {

    private float yaw;
    private float pitch;
    private boolean onGround;

    public PlayerLook12Packet() {}

    @Override
    public void write(ByteBuf out, int protocolVersion) {

    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {
        this.yaw = in.readFloat();
        this.pitch = in.readFloat();
        this.onGround = in.readBoolean();
    }

    public float getYaw() { return yaw; }
    public float getPitch() { return pitch; }
    public boolean isOnGround() { return onGround; }

    @Override
    public int getPacketId(int protocolVersion) {
        return 0x0C;
    }
}