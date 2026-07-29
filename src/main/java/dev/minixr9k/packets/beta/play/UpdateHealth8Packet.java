package dev.minixr9k.packets.beta.play;

import dev.minixr9k.utils.BetaPacket;
import io.netty.buffer.ByteBuf;

public class UpdateHealth8Packet implements BetaPacket {

    private short health;

    public UpdateHealth8Packet() {}

    public UpdateHealth8Packet(short health) {
        this.health = health;
    }

    @Override
    public void write(ByteBuf out, int protocolVersion) {
        out.writeShort(health);
    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {
        this.health = in.readShort();
    }

    public short getHealth() {
        return health;
    }

    @Override
    public int getPacketId(int protocolVersion) {
        return 0x08;
    }
}