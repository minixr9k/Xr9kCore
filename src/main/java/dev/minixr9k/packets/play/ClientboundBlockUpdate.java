package dev.minixr9k.packets.play;

import dev.minixr9k.utils.MinecraftPacket;
import io.netty.buffer.ByteBuf;

import static dev.minixr9k.utils.ProtocolUtils.writeVarInt;

public class ClientboundBlockUpdate implements MinecraftPacket {

    private final int x;
    private final int y;
    private final int z;
    private final int blockId;

    public ClientboundBlockUpdate(int x, int y, int z, int blockId) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.blockId = blockId;
    }

    @Override
    public void write(ByteBuf out, int protocolVersion) {
        long position = (((long) x & 0x3FFFFFF) << 38) |
                (((long) z & 0x3FFFFFF) << 12) |
                ((long) y & 0xFFF);

        out.writeLong(position);

        writeVarInt(out, blockId);
    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {

    }

    @Override
    public int getPacketId(int protocolVersion) {
        if (protocolVersion >= 770)
            return 0x08;
        else
            return 0x09;
    }
}
