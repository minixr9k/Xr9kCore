package dev.minixr9k.registries;

import com.google.gson.stream.JsonReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

public class BlockRegistry {

    private static int[] hashes;
    private static int[] ids;
    private static boolean isInitialized = false;

    public static void initialize() {
        if (isInitialized) return;

        // Используем TreeMap для временной сортировки по хешу
        TreeMap<Integer, Integer> tempMap = new TreeMap<>();
        String fileName = "/blocks/blocks1.21.8.json";

        try (InputStream is = BlockRegistry.class.getResourceAsStream(fileName)) {
            if (is == null) throw new RuntimeException("Файл не найден: " + fileName);

            try (JsonReader reader = new JsonReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                reader.beginObject();
                while (reader.hasNext()) {
                    String blockName = reader.nextName();
                    int defaultId = -1;

                    reader.beginObject();
                    while (reader.hasNext()) {
                        String key = reader.nextName();
                        if (key.equals("states")) {
                            reader.beginArray();
                            while (reader.hasNext()) {
                                int id = -1;
                                boolean isDefault = false;
                                reader.beginObject();
                                while (reader.hasNext()) {
                                    String attr = reader.nextName();
                                    if (attr.equals("id")) id = reader.nextInt();
                                    else if (attr.equals("default")) isDefault = reader.nextBoolean();
                                    else reader.skipValue();
                                }
                                reader.endObject();
                                if (isDefault) defaultId = id;
                                else if (defaultId == -1) defaultId = id;
                            }
                            reader.endArray();
                        } else {
                            reader.skipValue();
                        }
                    }
                    reader.endObject();

                    // Заполняем временную мапу
                    if (defaultId != -1) {
                        tempMap.put(blockName.hashCode(), defaultId);
                    }
                }
                reader.endObject();
            }

            // Переносим в примитивные массивы
            hashes = new int[tempMap.size()];
            ids = new int[tempMap.size()];
            int i = 0;
            for (Map.Entry<Integer, Integer> entry : tempMap.entrySet()) {
                hashes[i] = entry.getKey();
                ids[i] = entry.getValue();
                i++;
            }

            isInitialized = true;
            System.out.println("[BlockRegistry] Успешно загружено " + hashes.length + " блоков (Memory Optimized).");
        } catch (Exception e) {
            throw new RuntimeException("Ошибка инициализации BlockRegistry", e);
        }
    }

//    public static int getBlock(String block, int protocolVersion) {
//        if (!isInitialized) initialize();
//
//        if (!block.contains(":")) block = "minecraft:" + block;
//
//        // Бинарный поиск работает за O(log n)
//        int index = Arrays.binarySearch(hashes, block.hashCode());
//        return (index >= 0) ? ids[index] : -1;
//    }

    public static int getBlock(String block, int protocolVersion) {
        if (!isInitialized) initialize();
        if (!block.contains(":")) block = "minecraft:" + block;

        // Убираем состояние [axis=..., type=... и т.д.]
        String cleanBlock = block;
        if (block.contains("[")) {
            cleanBlock = block.substring(0, block.indexOf('['));
        }

        int index = Arrays.binarySearch(hashes, cleanBlock.hashCode());
        return (index >= 0) ? ids[index] : -1;
    }
}