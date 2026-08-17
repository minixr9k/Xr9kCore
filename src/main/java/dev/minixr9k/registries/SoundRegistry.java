package dev.minixr9k.registries;

import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class SoundRegistry {

    public static int getSound(String sound, int protocolVersion) {
        String fileName = "sounds1.21.8.json";

        try (InputStream is = SoundRegistry.class.getResourceAsStream("/sounds/" + fileName)) {
            if (is == null) return 0;

            try (JsonReader reader = new JsonReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                return findIdInStream(reader, sound);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private static int findIdInStream(JsonReader reader, String targetEntity) throws Exception {
        int pigId = -1; // Резервный ID свиньи на случай, если нужная сущность не найдена

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("minecraft:sound_event")) {
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