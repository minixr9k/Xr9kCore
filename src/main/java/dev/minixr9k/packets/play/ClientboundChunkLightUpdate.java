package dev.minixr9k.packets.play;

import dev.minixr9k.utils.MinecraftPacket;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import static dev.minixr9k.utils.ProtocolUtils.writeVarInt;

public class ClientboundChunkLightUpdate implements MinecraftPacket {

    private final int chunkX;
    private final int chunkZ;

    public ClientboundChunkLightUpdate(int chunkX, int chunkZ) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }

    @Override
    public void write(ByteBuf out, int protocolVersion) {
        writeVarInt(out, chunkX);
        writeVarInt(out, chunkZ);

        // Всего в нашем мире 32 секции. Маска должна покрывать 32 + 2 = 34 секции.
        // Индекс 0 — это секция под миром (Y = -5). Индексы 1..32 — игровые секции. Индекс 33 — над миром.

        // Создаем маски
        long skyLightMask = 0;
        long blockLightMask = 0;
        long emptySkyLightMask = 0;
        long emptyBlockLightMask = 0;

        // Включаем Sky Light (солнечный свет) для ВСЕХ 34 секций, чтобы весь мир был освещен сверху
        for (int i = 0; i < 34; i++) {
            skyLightMask |= (1L << i);
        }

        // Оптимизация: вместо отправки массивов Block Light, помечаем все 34 секции как "пустые по свету блоков"
        // Клиент занулит свет от факелов сам, а мы сэкономим кучу трафика!
        for (int i = 0; i < 34; i++) {
            emptyBlockLightMask |= (1L << i);
        }

        // Записываем BitSet'ы в буфер (в Майнкрафте BitSet — это VarInt длины массива лонгов + сами лонги)
        writeMinecraftBitSet(out, skyLightMask);
        writeMinecraftBitSet(out, blockLightMask);
        writeMinecraftBitSet(out, emptySkyLightMask);
        writeMinecraftBitSet(out, emptyBlockLightMask);

        // --- 1. Пишем массивы SKY LIGHT ---
        // Так как в skyLightMask у нас горит 34 бита, мы ОБЯЗАНЫ отправить ровно 34 массива
        writeVarInt(out, 34); // Количество массивов

        byte[] fullSkyLight = new byte[2048];
        Arrays.fill(fullSkyLight, (byte) 0xFF); // Максимальное солнце (15 уровень света для всех блоков)

        for (int i = 0; i < 34; i++) {
            writeVarInt(out, 2048); // Длина массива (Prefixed Array)
            out.writeBytes(fullSkyLight);
        }

        // --- 2. Пишем массивы BLOCK LIGHT ---
        // Так как blockLightMask пустая (мы использовали emptyBlockLightMask), слать массивы не нужно!
        writeVarInt(out, 0); // 0 массивов блоков
    }

    /**
     * Утилита для записи маски в формате BitSet Майнкрафта
     */
    private void writeMinecraftBitSet(ByteBuf out, long mask) {
        if (mask == 0) {
            writeVarInt(out, 0); // Длина массива лонгов = 0
        } else {
            writeVarInt(out, 1); // Нам хватает одного лонга, чтобы уместить 34 бита
            out.writeLong(mask);
        }
    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {}

    @Override
    public int getPacketId(int protocolVersion) {
        if (protocolVersion >= 773) {
            return 0x2F;
        } else if (protocolVersion >= 770) {
            return 0x2A;
        } else if (protocolVersion <= 767) {
            return 0x2A;
        } else {
            return 0x2B;
        }
    }
}