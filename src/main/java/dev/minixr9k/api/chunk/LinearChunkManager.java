package dev.minixr9k.api.chunk;

import com.github.luben.zstd.Zstd;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class LinearChunkManager {

    private static final int REGION_SIZE = 32; // Регион состоит из 32x32 чанков
    private static final int CHUNKS_PER_REGION = REGION_SIZE * REGION_SIZE; // 1024

    public static void saveWorld(Map<String, Chunk> chunks, File saveDir) throws IOException {
        if (!saveDir.exists()) {
            saveDir.mkdirs();
        }

        // Группируем чанки по файлам регионов. Координаты региона: rx = cx >> 5, rz = cz >> 5
        Map<String, Map<String, Chunk>> regionsMap = new HashMap<>();

        for (Chunk chunk : chunks.values()) {
            int rx = chunk.getX() >> 5;
            int rz = chunk.getZ() >> 5;
            String regionKey = rx + "," + rz;

            regionsMap.computeIfAbsent(regionKey, k -> new HashMap<>())
                    .put(chunk.getX() + "," + chunk.getZ(), chunk);
        }

        // Сохраняем каждый регион отдельно
        for (Map.Entry<String, Map<String, Chunk>> entry : regionsMap.entrySet()) {
            String[] parts = entry.getKey().split(",");
            int rx = Integer.parseInt(parts[0]);
            int rz = Integer.parseInt(parts[1]);

            File regionFile = new File(saveDir, "r." + rx + "." + rz + ".linear");
            saveRegion(regionFile, rx, rz, entry.getValue());
        }
    }

    private static void saveRegion(File file, int rx, int rz, Map<String, Chunk> regionChunks) throws IOException {
        // Выделяем буфер для заголовка (1024 чанка * 4 байта под смещение = 4096 байт)
        ByteBuf headerBuf = Unpooled.buffer(CHUNKS_PER_REGION * 4);
        // Буфер для сырых несжатых данных чанков
        ByteBuf uncompressedDataBuf = Unpooled.buffer();

        int[] offsets = new int[CHUNKS_PER_REGION];

        try {
            int currentOffset = 0;

            // Обходим сетку 32x32
            for (int cz = 0; cz < REGION_SIZE; cz++) {
                for (int cx = 0; cx < REGION_SIZE; cx++) {
                    int globalCX = (rx << 5) + cx;
                    int globalCZ = (rz << 5) + cz;
                    int index = cz * REGION_SIZE + cx;

                    Chunk chunk = regionChunks.get(globalCX + "," + globalCZ);

                    if (chunk == null) {
                        offsets[index] = -1; // Чанка нет
                        continue;
                    }

                    // Фиксируем текущее смещение в буфере несжатых данных
                    offsets[index] = currentOffset;

                    // Пишем чанк в uncompressedDataBuf
                    int startIdx = uncompressedDataBuf.writerIndex();

                    uncompressedDataBuf.writeInt(chunk.getX());
                    uncompressedDataBuf.writeInt(chunk.getZ());
                    uncompressedDataBuf.writeBoolean(chunk.isEmpty());

                    short[][] sections = chunk.getSections();
                    for (int i = 0; i < 24; i++) {
                        short[] section = sections[i];
                        if (section == null) {
                            uncompressedDataBuf.writeBoolean(false); // Секция пуста
                        } else {
                            uncompressedDataBuf.writeBoolean(true);  // Секция существует
                            for (short block : section) {
                                uncompressedDataBuf.writeShort(block); // 4096 блоков * 2 байта
                            }
                        }
                    }

                    // Вычисляем, сколько байт занял этот чанк
                    currentOffset += (uncompressedDataBuf.writerIndex() - startIdx);
                }
            }

            // Заполняем заголовок смещениями
            for (int offset : offsets) {
                headerBuf.writeInt(offset);
            }

            // Переводим ByteBuf в обычные массивы байт для сжатия Zstandard
            byte[] headerBytes = new byte[headerBuf.readableBytes()];
            headerBuf.readBytes(headerBytes);

            byte[] uncompressedBytes = new byte[uncompressedDataBuf.readableBytes()];
            uncompressedDataBuf.readBytes(uncompressedBytes);

            // Сжимаем данные блоков с помощью Zstd (level 3 — баланс скорости и сжатия)
            byte[] compressedBytes = Zstd.compress(uncompressedBytes, 3);

            // Склеиваем заголовок и сжатые данные в один итоговый файл
            byte[] finalFileBytes = new byte[headerBytes.length + compressedBytes.length];
            System.arraycopy(headerBytes, 0, finalFileBytes, 0, headerBytes.length);
            System.arraycopy(compressedBytes, 0, finalFileBytes, headerBytes.length, compressedBytes.length);

            Files.write(file.toPath(), finalFileBytes);
            System.out.println("[Core/World] Успешно сохранен регион: " + file.getName() + " | Размер: " + finalFileBytes.length + " байт");

        } finally {
            headerBuf.release();
            uncompressedDataBuf.release();
        }
    }

    /**
     * Загружает все регионы из папки Linear обратно в ОЗУ.
     */
    public static Map<String, Chunk> loadWorld(File saveDir) throws IOException {
        Map<String, Chunk> loadedChunks = new HashMap<>();
        if (!saveDir.exists()) return loadedChunks;

        File[] files = saveDir.listFiles((dir, name) -> name.startsWith("r.") && name.endsWith(".linear"));
        if (files == null) return loadedChunks;

        for (File file : files) {
            Map<String, Chunk> regionChunks = loadRegion(file);
            loadedChunks.putAll(regionChunks);
        }

        return loadedChunks;
    }

    private static Map<String, Chunk> loadRegion(File file) throws IOException {
        Map<String, Chunk> regionChunks = new HashMap<>();
        byte[] fileBytes = Files.readAllBytes(file.toPath());

        if (fileBytes.length < 4096) {
            throw new IOException("Файл Linear региона слишком мал или поврежден: " + file.getName());
        }

        // 1. Извлекаем заголовок (первые 4096 байт)
        ByteBuf headerBuf = Unpooled.wrappedBuffer(fileBytes, 0, 4096);
        int[] offsets = new int[CHUNKS_PER_REGION];
        for (int i = 0; i < CHUNKS_PER_REGION; i++) {
            offsets[i] = headerBuf.readInt();
        }
        headerBuf.release();

        // 2. Распаковываем оставшуюся сжатую ZSTD-часть
        byte[] compressedBytes = new byte[fileBytes.length - 4096];
        System.arraycopy(fileBytes, 4096, compressedBytes, 0, compressedBytes.length);

        // Узнаем размер оригинальных данных и декомпрессируем
        long decompressedSize = Zstd.decompressedSize(compressedBytes);
        byte[] uncompressedBytes = Zstd.decompress(compressedBytes, (int) decompressedSize);

        ByteBuf uncompressedBuf = Unpooled.wrappedBuffer(uncompressedBytes);

        try {
            // 3. Восстанавливаем чанки по смещениям
            for (int i = 0; i < CHUNKS_PER_REGION; i++) {
                int offset = offsets[i];
                if (offset == -1) continue; // Этого чанка не было на диске

                // Прыгаем на смещение чанка в буфере
                uncompressedBuf.readerIndex(offset);

                int cx = uncompressedBuf.readInt();
                int cz = uncompressedBuf.readInt();
                boolean isEmpty = uncompressedBuf.readBoolean();

                Chunk chunk = new Chunk(cx, cz, isEmpty);

                // Загружаем секции
                short[][] sections = chunk.getSections();
                for (int s = 0; s < 24; s++) {
                    boolean hasSection = uncompressedBuf.readBoolean();
                    if (hasSection) {
                        short[] sectionBlocks = new short[4096];
                        for (int b = 0; b < 4096; b++) {
                            sectionBlocks[b] = uncompressedBuf.readShort();
                        }
                        sections[s] = sectionBlocks;
                    } else {
                        sections[s] = null;
                    }
                }

                regionChunks.put(cx + "," + cz, chunk);
            }
        } finally {
            uncompressedBuf.release();
        }

        System.out.println("[Core/World] Загружен регион: " + file.getName() + " | Найдено чанков: " + regionChunks.size());
        return regionChunks;
    }
}