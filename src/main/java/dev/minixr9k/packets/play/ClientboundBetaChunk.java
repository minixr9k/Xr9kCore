package dev.minixr9k.packets.play;

import dev.minixr9k.utils.MinecraftPacket;
import io.netty.buffer.ByteBuf;
import static dev.minixr9k.utils.ProtocolUtils.writeVarInt;

public class ClientboundBetaChunk implements MinecraftPacket {

    private final int chunkX;
    private final int chunkZ;
    private static final int WATER_LEVEL = 64; // Тот самый уровень океана из беты

    // Таблица перестановок для честного Perlin Noise
    private static final int[] p = new int[512];
    private static final int[] permutation = { 151,160,137,91,90,15,
            131,13,201,95,96,53,194,233,7,225,140,36,103,30,69,142,8,99,37,240,21,10,23,
            190, 6,148,247,120,234,75,0,26,197,62,94,252,219,203,117,35,11,32,57,177,33,
            88,237,149,56,87,174,20,125,136,171,168, 68,175,74,165,71,134,139,48,27,166,
            77,146,158,231,83,111,229,122,60,211,133,230,220,105,92,41,55,46,245,40,244,
            102,143,54, 65,25,63,161, 1,216,80,73,209,76,132,187,208, 89,18,169,200,196,
            135,130,116,188,159,86,164,100,109,198,173,186, 3,64,52,217,226,250,124,123,
            5,202,38,147,118,126,255,82,85,212,207,206,59,227,47,16,58,17,182,189,28,42,
            223,183,170,213,119,248,152, 2,44,154,163, 70,221,153,101,155,167, 43,172,9,
            129,22,39,253, 19,98,108,110,79,113,224,232,178,185, 112,104,218,246,97,228,
            251,34,242,193,238,210,144,12,191,179,162,241, 81,51,145,235,249,14,239,107,
            49,192,214, 31,181,199,106,157,184, 84,204,176,115,121,50,45,127, 4,150,254,
            138,236,205,93,222,114,67,29,24,72,243,141,128,195,78,66,215,61,156,180
    };

    static {
        for (int i=0; i < 256 ; i++) p[256+i] = p[i] = permutation[i];
    }

    public ClientboundBetaChunk(int chunkX, int chunkZ) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }

    @Override
    public void write(ByteBuf out, int protocolVersion) {
        out.writeInt(chunkX);
        out.writeInt(chunkZ);

        writeVarInt(out, 0); // Heightmaps

        ByteBuf chunkDataBuf = out.alloc().buffer();
        try {
            int[][] heightMap = new int[16][16];
            int globalMinY = -64;

            // Генерация карты высот через фрактальный Перлин Шум (4 октавы для крутых скал)
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    double absX = (chunkX * 16) + x;
                    double absZ = (chunkZ * 16) + z;

                    double noise = 0;
                    double amplitude = 45.0; // Высота гор
                    double frequency = 0.006;

                    for (int o = 0; o < 4; o++) {
                        noise += perlin(absX * frequency, absZ * frequency) * amplitude;
                        amplitude *= 0.4;
                        frequency *= 3.0;
                    }

                    // Базовый уровень суши опускаем/поднимаем вокруг WATER_LEVEL
                    int height = (int) (62 + noise);
                    if (height < -64) height = -64;
                    if (height > 319) height = 319;
                    heightMap[x][z] = height;
                }
            }

            // Рендерим 32 секции
            for (int sectionY = 0; sectionY < 32; sectionY++) {
                int sectionStartActualY = globalMinY + (sectionY * 16);

                // Оптимизация под глубокий камень (с учетом уровня дна океанов)
                if (isFullyStone(sectionStartActualY, heightMap)) {
                    chunkDataBuf.writeShort(4096);
                    chunkDataBuf.writeByte(0);
                    writeVarInt(chunkDataBuf, 1); // stone
                    chunkDataBuf.writeByte(0);
                    writeVarInt(chunkDataBuf, 0);
                    continue;
                }

                // Оптимизация под чистый воздух на большой высоте
                if (isFullyAir(sectionStartActualY, heightMap)) {
                    chunkDataBuf.writeShort(0);
                    chunkDataBuf.writeByte(0);
                    writeVarInt(chunkDataBuf, 0); // air
                    chunkDataBuf.writeByte(0);
                    writeVarInt(chunkDataBuf, 0);
                    continue;
                }

                long[] sectionLongs = new long[256];
                int nonAirCount = 0;

                for (int y = 0; y < 16; y++) {
                    int actualY = sectionStartActualY + y;

                    for (int z = 0; z < 16; z++) {
                        for (int x = 0; x < 16; x++) {
                            int surfaceY = heightMap[x][z];
                            int localPaletteIndex = 0; // Воздух (ID 0)

                            if (actualY <= surfaceY) {
                                // Блоки суши
                                nonAirCount++;
                                if (surfaceY <= WATER_LEVEL + 2) {
                                    // 1. Пляжная зона (Песок у воды)
                                    if (actualY >= surfaceY - 2) {
                                        localPaletteIndex = 4; // Песок (Индекс 4)
                                    } else {
                                        localPaletteIndex = 1; // Ниже песка — Камень
                                    }
                                } else {
                                    // 2. Классический бета-пирог суши
                                    if (actualY == surfaceY) {
                                        localPaletteIndex = 3; // Трава (Индекс 3)[cite: 1]
                                    } else if (actualY >= surfaceY - 3) {
                                        localPaletteIndex = 2; // Земля (Индекс 2)
                                    } else {
                                        localPaletteIndex = 1; // Камень (Индекс 1)
                                    }
                                }
                            } else if (actualY <= WATER_LEVEL) {
                                // 3. Водная гладь (Если выше суши, но ниже уровня моря)
                                nonAirCount++;
                                localPaletteIndex = 5; // Вода (Индекс 5)
                            }

                            int blockIndex = (y * 256) + (z * 16) + x;
                            int longIndex = blockIndex / 16;
                            int bitOffset = (blockIndex % 16) * 4;
                            sectionLongs[longIndex] |= ((long) localPaletteIndex << bitOffset);
                        }
                    }
                }

                chunkDataBuf.writeShort(nonAirCount);
                chunkDataBuf.writeByte(4); // BPE = 4

                // Расширенная локальная палитра (6 элементов)
                writeVarInt(chunkDataBuf, 6);
                writeVarInt(chunkDataBuf, 0);   // 0: air
                writeVarInt(chunkDataBuf, 1);   // 1: stone
                writeVarInt(chunkDataBuf, 10);  // 2: dirt
                writeVarInt(chunkDataBuf, 9);   // 3: grass_block[cite: 1]
                writeVarInt(chunkDataBuf, 118); // 4: sand (ID 118)
                writeVarInt(chunkDataBuf, 101); // 5: water (ID 101)

                for (long l : sectionLongs) {
                    chunkDataBuf.writeLong(l);
                }

                chunkDataBuf.writeByte(0);
                writeVarInt(chunkDataBuf, 0);
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

        // Block entities & Lights
        writeVarInt(out, 0);
        for (int i = 0; i < 4; i++) {
            writeVarInt(out, 1);
            out.writeLong(0L);
        }
        writeVarInt(out, 0);
        writeVarInt(out, 0);
    }

    private boolean isFullyStone(int startY, int[][] heightMap) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                // Секция полностью каменная, если её верх ниже уровня земли (с запасом под землю/песок)
                if (startY + 15 > heightMap[x][z] - 4) return false;
            }
        }
        return true;
    }

    private boolean isFullyAir(int startY, int[][] heightMap) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                // Воздух, если секция выше и земли, и уровня воды
                if (startY <= heightMap[x][z] || startY <= WATER_LEVEL) return false;
            }
        }
        return true;
    }

    // Классический 2D математический Perlin Noise
    private static double perlin(double x, double y) {
        int X = (int)Math.floor(x) & 255;
        int Y = (int)Math.floor(y) & 255;
        x -= Math.floor(x);
        y -= Math.floor(y);
        double u = fade(x);
        double v = fade(y);
        int A = p[X]+Y, B = p[X+1]+Y;
        return lerp(v, lerp(u, grad(p[A], x, y), grad(B, x-1, y)),
                lerp(u, grad(p[A+1], x, y-1), grad(p[B+1], x-1, y-1)));
    }
    private static double fade(double t) { return t * t * t * (t * (t * 6 - 15) + 10); }
    private static double lerp(double t, double a, double b) { return a + t * (b - a); }
    private static double grad(int hash, double x, double y) {
        int h = hash & 7;
        double u = h < 4 ? x : y;
        double v = h < 4 ? y : x;
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v * 2.0 : -v * 2.0);
    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {}

    @Override
    public int getPacketId(int protocolVersion) {
        if (protocolVersion >= 773) return 0x2C;
        else if (protocolVersion >= 770) return 0x27;
        else if (protocolVersion <= 767) return 0x27;
        else return 0x28;
    }
}