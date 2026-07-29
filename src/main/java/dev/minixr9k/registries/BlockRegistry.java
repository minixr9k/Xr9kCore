package dev.minixr9k.registries;

import com.google.gson.stream.JsonReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class BlockRegistry {

    private static final Map<String, Integer> stateToId = new HashMap<>();
    private static final Map<String, Integer> defaultNameToId = new HashMap<>();
    private static final Map<Integer, String> idToState = new HashMap<>();
    private static boolean isInitialized = false;

    public static void initialize() {
        if (isInitialized) return;

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
                                Map<String, String> props = new TreeMap<>(); // TreeMap для сохранения сортировки ключей!

                                reader.beginObject();
                                while (reader.hasNext()) {
                                    String attr = reader.nextName();
                                    if (attr.equals("id")) {
                                        id = reader.nextInt();
                                    } else if (attr.equals("default")) {
                                        isDefault = reader.nextBoolean();
                                    } else if (attr.equals("properties")) {
                                        reader.beginObject();
                                        while (reader.hasNext()) {
                                            props.put(reader.nextName(), reader.nextString());
                                        }
                                        reader.endObject();
                                    } else {
                                        reader.skipValue();
                                    }
                                }
                                reader.endObject();

                                if (id != -1) {
                                    // Сохраняем полный блокстейт
                                    String fullState = buildStateString(blockName, props);
                                    stateToId.put(fullState, id);
                                    idToState.put(id, fullState);

                                    if (isDefault || defaultId == -1) {
                                        defaultId = id;
                                    }
                                }
                            }
                            reader.endArray();
                        } else {
                            reader.skipValue();
                        }
                    }
                    reader.endObject();

                    if (defaultId != -1) {
                        defaultNameToId.put(blockName, defaultId);
                    }
                }
                reader.endObject();
            }

            isInitialized = true;
            System.out.println("[BlockRegistry] Успешно загружено " + stateToId.size() + " состояний блоков.");
        } catch (Exception e) {
            throw new RuntimeException("Ошибка инициализации BlockRegistry", e);
        }
    }

    public static int getBlock(String block, int protocolVersion) {
        if (!isInitialized) initialize();
        if (!block.contains(":")) block = "minecraft:" + block;

        // 1. Точное совпадение со свойствами (например, minecraft:spruce_stairs[facing=south,...])
        if (block.contains("[")) {
            String normalized = normalizeBlockState(block);
            if (stateToId.containsKey(normalized)) {
                return stateToId.get(normalized);
            }
            // Если с кастомными свойствами не нашли, берем базовое имя
            block = block.substring(0, block.indexOf('['));
        }

        // 2. Фолбэк на дефолтное состояние блока
        return defaultNameToId.getOrDefault(block, -1);
    }

    public static String getBlockName(int blockId, int protocolVersion) {
        if (!isInitialized) initialize();
        return idToState.get(blockId);
    }

    // Нормализация порядка свойств (facing=south,half=top... -> в алфавитном порядке)
    private static String normalizeBlockState(String input) {
        int bracketIndex = input.indexOf('[');
        if (bracketIndex == -1) return input;

        String baseName = input.substring(0, bracketIndex);
        String propsRaw = input.substring(bracketIndex + 1, input.length() - 1);

        Map<String, String> props = new TreeMap<>();
        for (String pair : propsRaw.split(",")) {
            String[] kv = pair.split("=");
            if (kv.length == 2) {
                props.put(kv[0].trim(), kv[1].trim());
            }
        }
        return buildStateString(baseName, props);
    }

    private static String buildStateString(String baseName, Map<String, String> props) {
        if (props.isEmpty()) return baseName;
        StringBuilder sb = new StringBuilder(baseName).append('[');
        boolean first = true;
        for (Map.Entry<String, String> entry : props.entrySet()) {
            if (!first) sb.append(',');
            sb.append(entry.getKey()).append('=').append(entry.getValue());
            first = false;
        }
        sb.append(']');
        return sb.toString();
    }
}