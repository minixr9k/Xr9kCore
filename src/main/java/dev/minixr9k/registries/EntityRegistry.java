package dev.minixr9k.registries;

import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class EntityRegistry {

    public static int getEntity(String entity, int protocolVersion) {
        String fileName = "entities1.21.1.json";

        if (protocolVersion == 768)
            fileName = "entities1.21.3.json";
        else if (protocolVersion == 769)
            fileName = "entities1.21.4.json";
        else if (protocolVersion == 770)
            fileName = "entities1.21.5.json";
        else if (protocolVersion == 771)
            fileName = "entities1.21.6.json";
        else if (protocolVersion == 772)
            fileName = "entities1.21.8.json";
        else if (protocolVersion == 773)
            fileName = "entities1.21.10.json";
        else if (protocolVersion == 774)
            fileName = "entities1.21.11.json";

        try (InputStream is = EntityRegistry.class.getResourceAsStream("/entity_type/" + fileName)) {
            if (is == null) return 0;

            try (JsonReader reader = new JsonReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                return findIdInStream(reader, entity);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private static int findIdInStream(JsonReader reader, String targetEntity) throws Exception {
        int pigId = 0; // Резервный ID свиньи на случай, если нужная сущность не найдена

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("minecraft:entity_type")) {
                reader.beginObject();
                while (reader.hasNext()) {
                    String subName = reader.nextName();
                    if (subName.equals("entries")) {

                        reader.beginObject();
                        while (reader.hasNext()) {
                            String entityName = reader.nextName();

                            if (entityName.equalsIgnoreCase(targetEntity)) {
                                // Нашли нужную сущность — мгновенно возвращаем ID!
                                return readProtocolId(reader);
                            } else if (entityName.equalsIgnoreCase("minecraft:pig")) {
                                // Запоминаем ID свиньи для фоллбека и переходим к следующей сущности
                                pigId = readProtocolId(reader);
                            } else {
                                // Пропускаем чужие сущности без создания объектов в памяти
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

        // Если искомая сущность не была найдена во всем файле, возвращаем ID свиньи
        return pigId;
    }

    private static int readProtocolId(JsonReader reader) throws IOException {
        int id = 0;
        reader.beginObject();
        while (reader.hasNext()) {
            String prop = reader.nextName();
            if (prop.equals("protocol_id")) {
                id = reader.nextInt();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return id;
    }
}