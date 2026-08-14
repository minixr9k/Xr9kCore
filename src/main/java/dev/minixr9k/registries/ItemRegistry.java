package dev.minixr9k.registries;

import com.google.gson.stream.JsonReader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ItemRegistry {

    public static int getItem(String targetItem, int protocolVersion) {
        String fileName = "items1.21.8.json";

        if (protocolVersion == 774)
            fileName = "items1.21.11.json";

        try (InputStream is = ItemRegistry.class.getResourceAsStream("/item_type/" + fileName)) {
            if (is == null) return 0; // Воздух по дефолту

            try (JsonReader reader = new JsonReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                return findIdInStream(reader, targetItem);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static String getItemName(int targetId, int protocolVersion) {
        String fileName = "items1.21.8.json";

        if (protocolVersion == 774)
            fileName = "items1.21.11.json";

        try (InputStream is = ItemRegistry.class.getResourceAsStream("/item_type/" + fileName)) {
            if (is == null) return "minecraft:air";

            try (JsonReader reader = new JsonReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                return findNameInStream(reader, targetId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "minecraft:air";
        }
    }

    private static int findIdInStream(JsonReader reader, String targetItem) throws Exception {
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("minecraft:item")) {
                reader.beginObject();
                while (reader.hasNext()) {
                    String subName = reader.nextName();
                    if (subName.equals("entries")) {

                        reader.beginObject();
                        while (reader.hasNext()) {
                            String itemName = reader.nextName();

                            if (itemName.equalsIgnoreCase(targetItem)) {
                                reader.beginObject();
                                while (reader.hasNext()) {
                                    String prop = reader.nextName();
                                    if (prop.equals("protocol_id")) {
                                        int id = reader.nextInt();
                                        return id;
                                    } else {
                                        reader.skipValue();
                                    }
                                }
                            } else {
                                reader.skipValue();
                            }
                        }
                        reader.endObject();
                    } else {
                        reader.skipValue();
                    }
                }
                reader.endObject();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();

        return 0;
    }

    private static String findNameInStream(JsonReader reader, int targetId) throws Exception {
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("minecraft:item")) {
                reader.beginObject();
                while (reader.hasNext()) {
                    String subName = reader.nextName();
                    if (subName.equals("entries")) {
                        reader.beginObject();
                        while (reader.hasNext()) {
                            String itemName = reader.nextName();
                            reader.beginObject();
                            while (reader.hasNext()) {
                                String prop = reader.nextName();
                                if (prop.equals("protocol_id")) {
                                    int id = reader.nextInt();
                                    if (id == targetId) {
                                        return itemName; // Нашли! Возвращаем название
                                    }
                                } else {
                                    reader.skipValue();
                                }
                            }
                            reader.endObject();
                        }
                        reader.endObject();
                    } else {
                        reader.skipValue();
                    }
                }
                reader.endObject();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return "minecraft:air"; // Если не нашли
    }

}
