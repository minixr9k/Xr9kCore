package dev.minixr9k.packets.beta.play;

import dev.minixr9k.utils.BetaPacket;
import io.netty.buffer.ByteBuf;

public class TimeUpdate4Packet implements BetaPacket {

    private long time;

    public TimeUpdate4Packet() {}

    public TimeUpdate4Packet(long time) {
        this.time = time;
    }

    @Override
    public void write(ByteBuf out, int protocolVersion) {
        out.writeLong(time);
    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {
        this.time = in.readLong();
    }

    public long getTime() {
        return time;
    }

    @Override
    public int getPacketId(int protocolVersion) {
        return 0x04;
    }
}