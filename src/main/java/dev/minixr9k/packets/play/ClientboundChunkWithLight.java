package dev.minixr9k.packets.play;

import dev.minixr9k.utils.MinecraftPacket;
import io.netty.buffer.ByteBuf;

import static dev.minixr9k.utils.ProtocolUtils.writeVarInt;

public class ClientboundChunkWithLight implements MinecraftPacket {

    private final int chunkX;
    private final int chunkZ;

    public ClientboundChunkWithLight(int chunkX, int chunkZ) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }

    @Override
    public void write(ByteBuf out, int protocolVersion) {
        out.writeInt(chunkX);
        out.writeInt(chunkZ);

        // Heightmaps NBT
        if (protocolVersion >= 770) {
            writeVarInt(out, 0);
        } else {
            out.writeByte(0x0A);
            out.writeByte(0x00);
        }

        ByteBuf chunkDataBuf = out.alloc().buffer();
        try {
            for (int i = 0; i < 32; i++) {
                chunkDataBuf.writeShort(0); // block count = 0
                chunkDataBuf.writeByte(0);  // blocks singular
                chunkDataBuf.writeByte(0);  // block id = 0 (air)
                chunkDataBuf.writeByte(0);  // biome singular

                if (protocolVersion >= 770) {
                    writeVarInt(chunkDataBuf, 0); // plains
                } else {
                    writeVarInt(chunkDataBuf, 0);
                }
            }

            chunkDataBuf.writeByte(0);
            chunkDataBuf.writeByte(0);
            chunkDataBuf.writeByte(0);
            writeVarInt(chunkDataBuf, 0);

            writeVarInt(out, chunkDataBuf.readableBytes());
            out.writeBytes(chunkDataBuf);
        } finally {
            if (chunkDataBuf.refCnt() > 0) {
                chunkDataBuf.release();
            }
        }

        // Block entities count
        writeVarInt(out, 0);

        // Light Data
        if (protocolVersion >= 770) {
            for (int i = 0; i < 4; i++) {
                writeVarInt(out, 1);
                out.writeLong(0L);
            }
        } else {
            writeVarInt(out, 0); // Sky Light Mask
            writeVarInt(out, 0); // Block Light Mask
            writeVarInt(out, 0); // Empty Sky Light Mask
            writeVarInt(out, 0); // Empty Block Light Mask
        }

        writeVarInt(out, 0); // Sky Light array count
        writeVarInt(out, 0); // Block Light array count
    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {}

    @Override
    public int getPacketId(int protocolVersion) {
        if (protocolVersion >= 773) {
            return 0x2C;
        } else if (protocolVersion >= 770) {
            return 0x27;
        } else if (protocolVersion <= 767) {
            return 0x27;
        } else {
            return 0x28;
        }
    }
}