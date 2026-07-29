package dev.minixr9k.packets.beta.play;

import dev.minixr9k.utils.BetaPacket;
import io.netty.buffer.ByteBuf;

public class SpawnPosition6Packet implements BetaPacket {

    private int x;
    private int y;
    private int z;

    public SpawnPosition6Packet() {}

    public SpawnPosition6Packet(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void write(ByteBuf out, int protocolVersion) {
        out.writeInt(x);
        out.writeInt(y);
        out.writeInt(z);
    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {

    }

    @Override
    public int getPacketId(int protocolVersion) {
        return 0x06;
    }
}