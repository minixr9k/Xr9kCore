package dev.minixr9k.packets.beta.play;

import dev.minixr9k.utils.BetaPacket;
import io.netty.buffer.ByteBuf;

public class PreChunk32Packet implements BetaPacket {

    private int x;
    private int z;
    private boolean mode;

    public PreChunk32Packet() {}

    public PreChunk32Packet(int x, int z, boolean mode) {
        this.x = x;
        this.z = z;
        this.mode = mode;
    }

    @Override
    public void write(ByteBuf out, int protocolVersion) {
        out.writeInt(x);
        out.writeInt(z);
        out.writeBoolean(mode);
    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {
        this.x = in.readInt();
        this.z = in.readInt();
        this.mode = in.readBoolean();
    }

    public int getX() { return x; }
    public int getZ() { return z; }
    public boolean isMode() { return mode; }

    @Override
    public int getPacketId(int protocolVersion) {
        return 0x32;
    }
}