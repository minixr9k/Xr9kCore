package dev.minixr9k.registries;

import com.google.gson.stream.JsonReader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ComponentsRegistry {

    public static int getComponent(String component, int protocolVersion) {
        String fileName = "components1.21.8.json";

        if (protocolVersion == 774)
            fileName = "components1.21.11.json";

        try (InputStream is = ItemRegistry.class.getResourceAsStream("/data_component_type/" + fileName)) {
            if (is == null) return -1;

            try (JsonReader reader = new JsonReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                return findIdInStream(reader, component);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static int findIdInStream(JsonReader reader, String targetItem) throws Exception {
        // Пропускаем обертку до начала объектов
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("minecraft:data_component_type")) {
                reader.beginObject();
                while (reader.hasNext()) {
                    String subName = reader.nextName();
                    if (subName.equals("entries")) {

                        // Мы зашли в массив предметов. Ищем нужный нам!
                        reader.beginObject();
                        while (reader.hasNext()) {
                            String itemName = reader.nextName();

                            if (itemName.equalsIgnoreCase(targetItem)) {
                                // Нашли нужный предмет! Заходим внутрь его свойств
                                reader.beginObject();
                                while (reader.hasNext()) {
                                    String prop = reader.nextName();
                                    if (prop.equals("protocol_id")) {
                                        int id = reader.nextInt();
                                        return id; // Нашли! Выходим, закрывая стрим
                                    } else {
                                        reader.skipValue();
                                    }
                                }
                            } else {
                                // Это не тот предмет, который мы ищем.
                                // skipValue() мгновенно пропускает весь внутренний блок предмета
                                // без создания Java-объектов в куче (Heap)
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

        return -1;
    }

}
