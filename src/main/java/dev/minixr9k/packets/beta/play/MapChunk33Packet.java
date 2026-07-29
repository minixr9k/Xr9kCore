package dev.minixr9k.packets.beta.play;

import dev.minixr9k.api.chunk.Chunk;
import dev.minixr9k.utils.BetaPacket;
import io.netty.buffer.ByteBuf;

import java.util.zip.Deflater;

public class MapChunk33Packet implements BetaPacket {

    private final Chunk chunk;

    public MapChunk33Packet(Chunk chunk) {
        this.chunk = chunk;
    }

    @Override
    public void write(ByteBuf out, int protocolVersion) {
        int chunkX = chunk.getX();
        int chunkZ = chunk.getZ();

        int blockX = chunkX * 16;
        short blockY = 0;
        int blockZ = chunkZ * 16;

        byte sizeX = 15;
        byte sizeY = 127;
        byte sizeZ = 15;

        // 32768 (Blocks) + 16384 (Data) + 16384 (BlockLight) + 16384 (SkyLight) = 81920 байт
        byte[] rawData = new byte[81920];

        short[][] sections = chunk.getSections();
        int minSectionY = -4; // Измени на 0, если координаты идут от Y=0!

        // Массив для хранения типа блока по координатам (чтобы потом легко рассчитать свет)
        byte[][][] blockGrid = new byte[16][128][16];

        // 1. Заполняем блоки
        for (int secIndex = 0; secIndex < sections.length; secIndex++) {
            short[] section = sections[secIndex];
            if (section == null) continue;

            int sectionBaseY = (secIndex + minSectionY) * 16;

            for (int y = 0; y < 16; y++) {
                int worldY = sectionBaseY + y;
                int betaY = worldY; // (worldY + 64), если нужно смещение

                if (betaY < 0 || betaY > 127) continue;

                for (int z = 0; z < 16; z++) {
                    for (int x = 0; x < 16; x++) {
                        int betaIndex = betaY + (z * 128) + (x * 128 * 16);
                        int sectionIndex = (y << 8) | (z << 4) | x;

                        byte sanitized = sanitizeBlockId(section[sectionIndex]);
                        rawData[betaIndex] = sanitized;
                        blockGrid[x][betaY][z] = sanitized;
                    }
                }
            }
        }

        // 2. Рассчитываем SkyLight (сбалансированные тени)
        int skyLightOffset = 32768 + 16384 + 16384; // 65536

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int currentLight = 15; // Солнечный свет над чанком

                for (int y = 127; y >= 0; y--) {
                    byte blockId = blockGrid[x][y][z];

                    if (blockId == 0) { // Воздух
                        // Свет не теряется
                    } else if (isSemiTransparent(blockId)) { // Листва, вода
                        // ⚙️ НАСТРОЙКА 1: Ослабление под деревьями/водой
                        // Было -3 (темно). Поставь -1 (очень мягко) или -2 (средне)
                        currentLight = Math.max(0, currentLight - 1);
                    } else { // Земля, камень, дерево
                        // ⚙️ НАСТРОЙКА 2: Свет под твёрдыми блоками/в пещерах
                        // Было 0 (абсолютная тьма). Поставь 2 или 3 для мягких теней!
                        currentLight = Math.min(currentLight, 2);
                    }

                    // Запись в полубайтовый массив
                    int nibbleIndex = (y >> 1) + (z * 64) + (x * 64 * 16);
                    int offset = skyLightOffset + nibbleIndex;

                    byte lightValue = (byte) (currentLight & 0x0F);

                    if ((y & 1) == 0) {
                        rawData[offset] = (byte) ((rawData[offset] & 0xF0) | lightValue);
                    } else {
                        rawData[offset] = (byte) ((rawData[offset] & 0x0F) | (lightValue << 4));
                    }
                }
            }
        }

        // 3. Сжатие
        Deflater deflater = new Deflater();
        deflater.setInput(rawData);
        deflater.finish();

        byte[] compressedData = new byte[81920];
        int compressedSize = deflater.deflate(compressedData);
        deflater.end();

        // 4. Запись в буфер
        out.writeInt(blockX);
        out.writeShort(blockY);
        out.writeInt(blockZ);
        out.writeByte(sizeX);
        out.writeByte(sizeY);
        out.writeByte(sizeZ);
        out.writeInt(compressedSize);
        out.writeBytes(compressedData, 0, compressedSize);
    }

    private boolean isSemiTransparent(byte blockId) {
        int id = blockId & 0xFF;
        // Листва (18), Вода (8, 9), Лед (79), Спаунер (52), Снег-слой (78)
        return id == 18 || id == 8 || id == 9 || id == 79 || id == 78;
    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {}

    @Override
    public int getPacketId(int protocolVersion) {
        return 0x33;
    }

    // Вспомогательный метод: пропускает ли блок свет
    private boolean isTransparent(byte blockId) {
        int id = blockId & 0xFF;
        // Воздух, стекло, листва, вода, факелы и т.д. пропускают свет
        return id == 0 || id == 18 || id == 20 || id == 8 || id == 9 || id == 50 || id == 31 || id == 37 || id == 38;
    }

    private byte sanitizeBlockId(short blockId) {
        int id = blockId & 0xFFFF;
        if (id == 0) return 0; // air
        if (id == 9) return 2; // grass_block
        if (id == 10) return 3; // dirt
        if (id == 14) return 4; // cobblestone
        if (id == 15) return 5; // oak_planks
        if (id == 85) return 5; // oak_planks
        if (id == 136) return 7; // bedrock
        if (id == 137) return 17; // oak_log
        if (id == 138) return 17; // oak_log
        if (id == 255) return 18; // oak_leaves
        if (id == 118) return 12; // sand
        if (id == 2141) return 46; // tnt
        if (id > 96) return 1; // Заменяем неизвестные беты блоки на камень (Stone)
        return (byte) id;
    }
}