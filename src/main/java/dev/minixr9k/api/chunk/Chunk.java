package dev.minixr9k.api.chunk;

import dev.minixr9k.config.Configuration;

public class Chunk {
    private final int x;
    private final int z;
    private boolean isEmpty;

    private final short[][] sections = new short[24][];

    public Chunk(int x, int z, boolean isEmpty) {
        this.x = x;
        this.z = z;
        this.isEmpty = isEmpty;

        if (!isEmpty)
            generateTerrain();
    }

    private void generateTerrain() {
        String worldType = Configuration.get().world.type;
        switch (worldType.toUpperCase()) {
            case "FLAT" -> {
                generateFlatTerrain();
            }
            case "FLAT/MINICORE" -> {
                generateDefaultTerrain();
            }
            default -> {
                generateFlatTerrain();
            }
        }
    }

    private void generateFlatTerrain() {
        int sectionTarget = 0;
        int startWorldY = (sectionTarget * 16) - 64;

        for (int localX = 0; localX < 16; localX++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                // Бедрок
                setBlockAt(localX, startWorldY, localZ, (short) 85);
                // Земля
                for (int y = 1; y <= 2; y++) {
                    setBlockAt(localX, startWorldY + y, localZ, (short) 10);
                }
                // Трава
                setBlockAt(localX, startWorldY + 3, localZ, (short) 9);
            }
        }
    }

    private void generateDefaultTerrain() {
        int sectionTarget = 7;
        int startWorldY = (sectionTarget * 16) - 64;

        for (int localX = 0; localX < 16; localX++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                // Камень (Y внутри секции 0..11)
                for (int y = 0; y <= 11; y++) {
                    setBlockAt(localX, startWorldY + y, localZ, (short) 1);
                }
                // Земля (Y внутри секции 12..14)
                for (int y = 12; y <= 14; y++) {
                    setBlockAt(localX, startWorldY + y, localZ, (short) 10);
                }
                // Трава (Y внутри секции 15)
                setBlockAt(localX, startWorldY + 15, localZ, (short) 9);
            }
        }
    }

    public void setBlockAt(int worldX, int worldY, int worldZ, short blockId) {
        int internalY = worldY + 64; // Смещаем диапазон -64..319 в 0..383
        if (internalY < 0 || internalY >= 384) return;

        // Определяем, в какую секцию попадает блок (0-23)
        int sectionIndex = internalY >> 4;
        int localY = internalY & 15;
        int localX = worldX & 15;
        int localZ = worldZ & 15;

        // Если блок не воздух, а чанк считался пустым — он больше не пустой
        if (blockId != 0 && isEmpty) {
            this.isEmpty = false;
        }

        // Инициализируем массив секции только при реальной записи блока!
        if (sections[sectionIndex] == null) {
            if (blockId == 0) return; // Зачем создавать секцию ради воздуха?
            sections[sectionIndex] = new short[16 * 16 * 16];
        }

        int blockIndex = (localY << 8) | (localZ << 4) | localX;
        sections[sectionIndex][blockIndex] = blockId;
    }

    public short getBlockAt(int worldX, int worldY, int worldZ) {
        if (isEmpty) return 0; // В пустом чанке всегда воздух

        int internalY = worldY + 64;
        if (internalY < 0 || internalY >= 384) return 0;

        int sectionIndex = internalY >> 4;
        if (sections[sectionIndex] == null) return 0; // Пустая секция = воздух

        int localY = internalY & 15;
        int localX = worldX & 15;
        int localZ = worldZ & 15;

        int blockIndex = (localY << 8) | (localZ << 4) | localX;
        return sections[sectionIndex][blockIndex];
    }

    public int getX() { return x; }
    public int getZ() { return z; }
    public boolean isEmpty() { return isEmpty; }
    public short[][] getSections() { return sections; }
}
