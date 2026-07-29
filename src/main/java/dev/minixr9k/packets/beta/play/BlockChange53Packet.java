package dev.minixr9k.packets.beta.play;

import dev.minixr9k.utils.BetaPacket;
import io.netty.buffer.ByteBuf;

public class BlockChange53Packet implements BetaPacket {

    private int x;
    private byte y;
    private int z;
    private byte blockType;
    private byte blockMetadata;

    public BlockChange53Packet() {}

    public BlockChange53Packet(int x, int y, int z, int blockType, int blockMetadata) {
        this.x = x;
        this.y = (byte) y;
        this.z = z;
        this.blockType = (byte) blockType;
        this.blockMetadata = (byte) blockMetadata;
    }

    public BlockChange53Packet(int x, int y, int z, int blockType) {
        this(x, y, z, blockType, 0);
    }

    @Override
    public void write(ByteBuf out, int protocolVersion) {
        out.writeInt(x);
        out.writeByte(y);
        out.writeInt(z);
        out.writeByte(blockType);
        out.writeByte(blockMetadata);
    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {
        this.x = in.readInt();
        this.y = in.readByte();
        this.z = in.readInt();
        this.blockType = in.readByte();
        this.blockMetadata = in.readByte();
    }

    public int getX() { return x; }
    public int getY() { return y & 0xFF; } // Делаем беззнаковым
    public int getZ() { return z; }
    public int getBlockType() { return blockType & 0xFF; }
    public int getBlockMetadata() { return blockMetadata & 0xFF; }

    @Override
    public int getPacketId(int protocolVersion) {
        return 0x35;
    }
}