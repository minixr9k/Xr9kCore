package dev.minixr9k.api.chunk;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public class AnvilChunkManager {

    private static final int SECTOR_SIZE = 4096;
    private static final int REGION_SIZE = 32;
    private static final int CHUNKS_PER_REGION = REGION_SIZE * REGION_SIZE; // 1024

    /**
     * Сохраняет карту чанков в стандартные .mca файлы формата Anvil.
     */
    public static void saveWorld(Map<String, Chunk> chunks, File saveDir) throws IOException {
        if (!saveDir.exists()) saveDir.mkdirs();

        Map<String, Map<String, Chunk>> regionsMap = new HashMap<>();

        for (Chunk chunk : chunks.values()) {
            int rx = chunk.getX() >> 5;
            int rz = chunk.getZ() >> 5;
            regionsMap.computeIfAbsent(rx + "," + rz, k -> new HashMap<>())
                    .put(chunk.getX() + "," + chunk.getZ(), chunk);
        }

        for (Map.Entry<String, Map<String, Chunk>> entry : regionsMap.entrySet()) {
            String[] parts = entry.getKey().split(",");
            int rx = Integer.parseInt(parts[0]);
            int rz = Integer.parseInt(parts[1]);

            File regionFile = new File(saveDir, "r." + rx + "." + rz + ".mca");
            saveRegion(regionFile, rx, rz, entry.getValue());
        }
    }

    private static void saveRegion(File file, int rx, int rz, Map<String, Chunk> regionChunks) throws IOException {
        // Используем RandomAccessFile, так как формат Anvil требует прыжков по секторам
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            raf.setLength(0); // Очищаем старый файл, если он был

            int[] locations = new int[CHUNKS_PER_REGION];
            int currentSector = 2; // Сектор 0 - смещения, Сектор 1 - таймстампы

            // 1. Записываем данные чанков, начиная со 2-го сектора (смещение 8192 байт)
            for (int i = 0; i < CHUNKS_PER_REGION; i++) {
                int cz = i / REGION_SIZE;
                int cx = i % REGION_SIZE;
                int globalCX = (rx << 5) + cx;
                int globalCZ = (rz << 5) + cz;

                Chunk chunk = regionChunks.get(globalCX + "," + globalCZ);

                if (chunk == null) {
                    locations[i] = 0; // Чанка нет
                    continue;
                }

                // Сжимаем данные чанка (используется Zlib)
                byte[] chunkData = serializeAndCompress(chunk);
                int length = chunkData.length + 1; // +1 байт для типа сжатия
                int sectorsNeeded = (length + 4 + SECTOR_SIZE - 1) / SECTOR_SIZE; // Округляем вверх до 4KB

                // Формируем 4-байтный заголовок смещения (3 байта оффсет + 1 байт кол-во секторов)
                locations[i] = (currentSector << 8) | (sectorsNeeded & 0xFF);

                // Прыгаем в нужный сектор и пишем чанк
                raf.seek((long) currentSector * SECTOR_SIZE);
                raf.writeInt(length);
                raf.writeByte(2); // 2 = Zlib (стандарт Minecraft)
                raf.write(chunkData);

                currentSector += sectorsNeeded;
            }

            // 2. Возвращаемся в начало и пишем Заголовки
            raf.seek(0);
            for (int loc : locations) {
                raf.writeInt(loc); // Таблица смещений
            }

            // Таблица времени генерации/изменения (просто ставим текущее время для существующих чанков)
            int timestamp = (int) (System.currentTimeMillis() / 1000L);
            for (int i = 0; i < CHUNKS_PER_REGION; i++) {
                raf.writeInt(locations[i] != 0 ? timestamp : 0);
            }

            System.out.println("[Anvil] Сохранен регион: " + file.getName() + " | Выделено секторов: " + currentSector);
        }
    }

    /**
     * Загружает все .mca регионы обратно в память.
     */
    public static Map<String, Chunk> loadWorld(File saveDir) throws IOException {
        Map<String, Chunk> loadedChunks = new HashMap<>();
        if (!saveDir.exists()) return loadedChunks;

        File[] files = saveDir.listFiles((dir, name) -> name.startsWith("r.") && name.endsWith(".mca"));
        if (files == null) return loadedChunks;

        for (File file : files) {
            loadedChunks.putAll(loadRegion(file));
        }

        return loadedChunks;
    }

    private static Map<String, Chunk> loadRegion(File file) throws IOException {
        Map<String, Chunk> chunks = new HashMap<>();
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            if (raf.length() < 8192) return chunks; // Файл слишком мал (нет даже хедеров)

            for (int i = 0; i < CHUNKS_PER_REGION; i++) {
                raf.seek(i * 4L); // Читаем таблицу смещений в 0-ом секторе
                int location = raf.readInt();
                if (location == 0) continue; // Чанк не сгенерирован

                int offset = (location >> 8) & 0xFFFFFF; // Смещение в 4KB секторах
                int sectors = location & 0xFF;           // Размер в 4KB секторах

                if (offset < 2) continue; // Битый файл, сектора 0 и 1 зарезервированы

                raf.seek((long) offset * SECTOR_SIZE);
                int length = raf.readInt(); // Длина полезной нагрузки
                if (length <= 1) continue;

                byte compressionType = raf.readByte();
                byte[] compressedData = new byte[length - 1];
                raf.readFully(compressedData);

                // Декомпрессия
                byte[] uncompressed;
                if (compressionType == 2) { // Zlib
                    uncompressed = decompressZlib(compressedData);
                } else if (compressionType == 1) { // GZip (использовался в очень старых версиях)
                    // Для полноты можно добавить поддержку, но обычно тут 2
                    throw new IOException("GZip compression not implemented in this proxy");
                } else if (compressionType == 3) { // Без сжатия
                    uncompressed = compressedData;
                } else {
                    continue; // Неизвестный формат
                }

                Chunk chunk = deserializeChunk(uncompressed);
                chunks.put(chunk.getX() + "," + chunk.getZ(), chunk);
            }
        }
        System.out.println("[Anvil] Загружен регион: " + file.getName() + " | Чанков: " + chunks.size());
        return chunks;
    }

    // ==========================================
    // Вспомогательные методы сериализации (Специфично для твоего Chunk)
    // ==========================================

    private static byte[] serializeAndCompress(Chunk chunk) throws IOException {
        ByteBuf buf = Unpooled.buffer();
        try {
            buf.writeInt(chunk.getX());
            buf.writeInt(chunk.getZ());
            buf.writeBoolean(chunk.isEmpty());

            short[][] sections = chunk.getSections();
            for (int i = 0; i < 24; i++) {
                short[] section = sections[i];
                if (section == null) {
                    buf.writeBoolean(false);
                } else {
                    buf.writeBoolean(true);
                    for (short block : section) {
                        buf.writeShort(block);
                    }
                }
            }

            byte[] uncompressed = new byte[buf.readableBytes()];
            buf.readBytes(uncompressed);

            // Сжимаем через Zlib
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (DeflaterOutputStream dos = new DeflaterOutputStream(baos)) {
                dos.write(uncompressed);
            }
            return baos.toByteArray();
        } finally {
            buf.release(); // Обязательно освобождаем память Netty
        }
    }

    private static Chunk deserializeChunk(byte[] uncompressed) {
        ByteBuf buf = Unpooled.wrappedBuffer(uncompressed);
        try {
            int cx = buf.readInt();
            int cz = buf.readInt();
            boolean isEmpty = buf.readBoolean();

            Chunk chunk = new Chunk(cx, cz, isEmpty);
            short[][] sections = chunk.getSections();

            for (int s = 0; s < 24; s++) {
                if (buf.readBoolean()) {
                    short[] section = new short[4096];
                    for (int b = 0; b < 4096; b++) {
                        section[b] = buf.readShort();
                    }
                    sections[s] = section;
                } else {
                    sections[s] = null;
                }
            }
            return chunk;
        } finally {
            buf.release();
        }
    }

    private static byte[] decompressZlib(byte[] compressed) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(compressed);
        try (InflaterInputStream iis = new InflaterInputStream(bais)) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = iis.read(buffer)) > 0) {
                baos.write(buffer, 0, len);
            }
            return baos.toByteArray();
        }
    }
}