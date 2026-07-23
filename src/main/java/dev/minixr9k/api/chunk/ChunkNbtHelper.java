package dev.minixr9k.api.chunk;

import org.cloudburstmc.nbt.NBTInputStream;
import org.cloudburstmc.nbt.NBTOutputStream;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtMapBuilder;
import org.cloudburstmc.nbt.NbtType;
import org.cloudburstmc.nbt.NbtUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChunkNbtHelper {

    /**
     * Преобразует сжатые/сырые NBT-байты из Linear-файла в объект Chunk.
     */
    public static Chunk deserializeChunk(byte[] chunkData) throws IOException {
        NbtMap nbt;
        try (NBTInputStream in = NbtUtils.createReader(new ByteArrayInputStream(chunkData))) {
            nbt = (NbtMap) in.readTag();
        }

        int cx = nbt.getInt("xPos", 0);
        int cz = nbt.getInt("zPos", 0);
        String status = nbt.getString("Status", "full");

        boolean isEmpty = "empty".equalsIgnoreCase(status);
        Chunk chunk = new Chunk(cx, cz, isEmpty);

        // Парсинг секций
        List<NbtMap> sections = nbt.getList("sections", NbtType.COMPOUND);
        if (sections != null) {
            for (NbtMap section : sections) {
                int y = section.getByte("Y", (byte) 0);
                NbtMap blockStates = (NbtMap) section.get("block_states");

                if (blockStates != null) {
                    List<NbtMap> palette = blockStates.getList("palette", NbtType.COMPOUND);
                    long[] data = blockStates.getLongArray("data");

                    // Массив на 4096 блоков (16x16x16) для текущей секции
                    short[] sectionBlocks = new short[4096];

                    // 1. Вытаскиваем имена блоков из палитры
                    String[] paletteNames = new String[palette.size()];
                    for (int i = 0; i < palette.size(); i++) {
                        paletteNames[i] = palette.get(i).getString("Name", "minecraft:air");
                    }

                    // 2. Если массив data пустой, значит секция состоит из 1 типа блоков (обычно воздух)
                    if (data == null || data.length == 0) {
                        short blockId = getInternalId(paletteNames[0]);
                        Arrays.fill(sectionBlocks, blockId);
                    } else {
                        // 3. Распаковываем long массив
                        // Кол-во бит на блок минимум 4, либо логарифм размера палитры
                        int bitsPerBlock = Math.max(4, (int) Math.ceil(Math.log(palette.size()) / Math.log(2)));
                        int blocksPerLong = 64 / bitsPerBlock;
                        int mask = (1 << bitsPerBlock) - 1;

                        for (int i = 0; i < 4096; i++) {
                            int longIndex = i / blocksPerLong;
                            int bitOffset = (i % blocksPerLong) * bitsPerBlock;

                            if (longIndex < data.length) {
                                int paletteIndex = (int) ((data[longIndex] >>> bitOffset) & mask);
                                short blockId = getInternalId(paletteNames[paletteIndex]);
                                sectionBlocks[i] = blockId;
                            }
                        }
                    }

                    // TODO: Передай полученный массив в свой чанк
                    // Например: chunk.setSectionBlocks(y, sectionBlocks);
                }
            }
        }

        return chunk;
    }

    /**
     * Преобразует объект Chunk в сырые NBT-байты для упаковывания в Linear.
     */
    public static byte[] serializeChunk(Chunk chunk) throws IOException {
        NbtMapBuilder builder = NbtMap.builder();
        builder.putInt("xPos", chunk.getX());
        builder.putInt("zPos", chunk.getZ());
        builder.putInt("DataVersion", 3465); // Версия данных (1.20.1+)
        builder.putString("Status", chunk.isEmpty() ? "empty" : "full");

        List<NbtMap> sectionsList = new ArrayList<>();

        // TODO: Проитерируй секции своего чанка.
        // В 1.18+ секции идут от Y = -4 до Y = 19
        for (int y = -4; y < 20; y++) {

            // Замени на получение массива из своего чанка (если null - пропускаем секцию)
            short[] sectionBlocks = null; // chunk.getSectionBlocks(y);

            if (sectionBlocks == null) continue;

            // 1. Формируем уникальную палитру
            List<Short> uniqueIds = new ArrayList<>();
            for (short id : sectionBlocks) {
                if (!uniqueIds.contains(id)) uniqueIds.add(id);
            }

            NbtMapBuilder blockStatesBuilder = NbtMap.builder();

            // 2. Создаем NBT палитры
            List<NbtMap> paletteNbt = new ArrayList<>();
            for (short id : uniqueIds) {
                paletteNbt.add(NbtMap.builder().putString("Name", getBlockName(id)).build());
            }
            blockStatesBuilder.putList("palette", NbtType.COMPOUND, paletteNbt);

            // 3. Если блоков больше одного типа, пакуем индексы в long[]
            if (uniqueIds.size() > 1) {
                int bitsPerBlock = Math.max(4, (int) Math.ceil(Math.log(uniqueIds.size()) / Math.log(2)));
                int blocksPerLong = 64 / bitsPerBlock;
                int mask = (1 << bitsPerBlock) - 1;

                long[] data = new long[(4096 + blocksPerLong - 1) / blocksPerLong];

                for (int i = 0; i < 4096; i++) {
                    short blockId = sectionBlocks[i];
                    long paletteIndex = uniqueIds.indexOf(blockId);

                    int longIndex = i / blocksPerLong;
                    int bitOffset = (i % blocksPerLong) * bitsPerBlock;

                    data[longIndex] |= (paletteIndex & mask) << bitOffset;
                }
                blockStatesBuilder.putLongArray("data", data);
            }

            NbtMapBuilder sectionBuilder = NbtMap.builder();
            sectionBuilder.putByte("Y", (byte) y);
            sectionBuilder.put("block_states", blockStatesBuilder.build());
            sectionsList.add(sectionBuilder.build());
        }

        builder.putList("sections", NbtType.COMPOUND, sectionsList);

        NbtMap nbt = builder.build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (NBTOutputStream out = NbtUtils.createWriter(baos)) {
            out.writeTag(nbt);
        }
        return baos.toByteArray();
    }

    // ========================================================================
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ (ИХ НУЖНО ПРИВЯЗАТЬ К ТВОЕМУ РЕЕСТРУ БЛОКОВ)
    // ========================================================================

    private static short getInternalId(String blockName) {
        // TODO: Вернуть твой внутренний short ID блока по его строковому названию.
        // Если блок неизвестен, возвращай ID воздуха (например, 0).
        if ("minecraft:air".equals(blockName)) return 0;
        if ("minecraft:stone".equals(blockName)) return 1;
        return 0; // fallback на воздух
    }

    private static String getBlockName(short internalId) {
        // TODO: Обратный процесс - по внутреннему ID отдаем имя блока для сохранения.
        if (internalId == 1) return "minecraft:stone";
        return "minecraft:air";
    }
}