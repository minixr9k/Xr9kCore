package dev.minixr9k.packets.play;

import dev.minixr9k.utils.MinecraftPacket;
import io.netty.buffer.ByteBuf;
import static dev.minixr9k.utils.ProtocolUtils.writeVarInt;

public class ClientboundTestChunkWithLight implements MinecraftPacket {

    private final int chunkX;
    private final int chunkZ;

    public ClientboundTestChunkWithLight(int chunkX, int chunkZ) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }

    @Override
    public void write(ByteBuf out, int protocolVersion) {
        out.writeInt(chunkX);
        out.writeInt(chunkZ);

        // 1. Heightmaps NBT (для 1.21.x)
        writeVarInt(out, 0);

        ByteBuf chunkDataBuf = out.alloc().buffer();
        try {
            for (int i = 0; i < 32; i++) {
                if (i == 10) { // чанк из травы земли и камня
                    chunkDataBuf.writeShort(4096);
                    chunkDataBuf.writeByte(4); // Bits Per Entry = 4

                    // Локальная палитра блоков (Размер: 3 элемента)
                    writeVarInt(chunkDataBuf, 3);
                    writeVarInt(chunkDataBuf, 1); // Индекс 0: minecraft:stone (Глобальный ID 1)
                    writeVarInt(chunkDataBuf, 10); // Индекс 1: minecraft:dirt (Глобальный ID 10)
                    writeVarInt(chunkDataBuf, 9); // Индекс 2: minecraft:grass_block (Глобальный ID 9)[cite: 1]

                    // 1. Слой Камня (Y = 0..11) -> 12 слоёв по 16 лонгов = 192 лонга
                    for (int j = 0; j < 192; j++) {
                        chunkDataBuf.writeLong(0L);
                    }

                    // 2. Слой Земли (Y = 12..14) -> 3 слоя по 16 лонгов = 48 лонгов
                    for (int j = 0; j < 48; j++) {
                        chunkDataBuf.writeLong(0x1111111111111111L);
                    }

                    // 3. Слой Травы (Y = 15 — самый верх секции) -> 1 слой = 16 лонгов
                    for (int j = 0; j < 16; j++) {
                        chunkDataBuf.writeLong(0x2222222222222222L);
                    }

                    // Биомы оставляем без изменений
                    chunkDataBuf.writeByte(0);
                    writeVarInt(chunkDataBuf, 0);  // биом plains
                }
//                if (i == 10) { // чанк с 1 слоем травы
//                    chunkDataBuf.writeShort(256); // 256 блоков
//                    chunkDataBuf.writeByte(4);     // blocks singular (BPE = 0)
//
//                    writeVarInt(chunkDataBuf, 2); // Размер палитры: 2 элемента
//                    writeVarInt(chunkDataBuf, 0); // Индекс 0: minecraft:air (глобальный ID 0)
//                    writeVarInt(chunkDataBuf, 9); // Индекс 1: minecraft:grass_block (глобальный ID 9)
//
//                    for (int j = 0; j < 16; j++) {
//                        chunkDataBuf.writeLong(0x1111111111111111L);
//                    }
//                    // Записываем оставшиеся 240 лонгов, забитых индексом 0 (воздух)
//                    for (int j = 0; j < 240; j++) {
//                        chunkDataBuf.writeLong(0L);
//                    }
//
//
//                    chunkDataBuf.writeByte(0);     // biome singular
//                    writeVarInt(chunkDataBuf, 0);  // биом plains
//                }
//                if (i == 10) { // Чанк из фул травы
//                    chunkDataBuf.writeShort(4096); // 4096 блоков
//                    chunkDataBuf.writeByte(0);     // blocks singular (BPE = 0)
//                    chunkDataBuf.writeByte(9);     // ID блока Травы = 9
//                    chunkDataBuf.writeByte(0);     // biome singular
//                    writeVarInt(chunkDataBuf, 0);  // биом plains
//                }
                else {
                    // Пустые секции воздуха
                    chunkDataBuf.writeShort(0);    // block count = 0
                    chunkDataBuf.writeByte(0);     // blocks singular
                    chunkDataBuf.writeByte(0);     // block id = 0 (air)
                    chunkDataBuf.writeByte(0);     // biome singular
                    writeVarInt(chunkDataBuf, 0);  // биом plains
                }
            }

            // Твой оригинальный хвост дата-буфера
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

        // 2. Block entities count
        writeVarInt(out, 0);

        // 3. Твоя рабочая запись масок света для 1.21.8
        for (int i = 0; i < 4; i++) {
            writeVarInt(out, 1);
            out.writeLong(0L);
        }

        // Массивы света (так как маски пустые, пишем 0)
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