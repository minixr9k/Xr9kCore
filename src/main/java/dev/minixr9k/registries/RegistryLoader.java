package dev.minixr9k.registries;

import com.google.gson.*;
import net.kyori.adventure.key.Key;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtMapBuilder;
import org.geysermc.mcprotocollib.protocol.data.game.RegistryEntry;
import org.geysermc.mcprotocollib.protocol.packet.configuration.clientbound.ClientboundRegistryDataPacket;

import java.io.InputStreamReader;
import java.util.*;

public class RegistryLoader {
    private static final Gson GSON = new GsonBuilder().create();

    public static Map<String, ClientboundRegistryDataPacket> loadAllRegistries(String filename) {
        Map<String, ClientboundRegistryDataPacket> packets = new LinkedHashMap<>();
        try (InputStreamReader reader = new InputStreamReader(
                RegistryLoader.class.getResourceAsStream(filename))) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);
            for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
                String registryKey = entry.getKey();
                JsonObject registryData = entry.getValue().getAsJsonObject();
                List<RegistryEntry> entries = parseRegistry(registryData);
                if (!entries.isEmpty()) {
                    packets.put(registryKey, new ClientboundRegistryDataPacket(Key.key(registryKey), entries));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return packets;
    }

    private static List<RegistryEntry> parseRegistry(JsonObject registryData) {
        JsonArray valueArray = registryData.getAsJsonArray("value");
        if (valueArray == null) return Collections.emptyList();

        List<RegistryEntry> entries = new ArrayList<>();
        for (JsonElement elem : valueArray) {
            JsonObject obj = elem.getAsJsonObject();
            String name = obj.get("name").getAsString();
            JsonObject element = obj.getAsJsonObject("element");
            NbtMap nbt = jsonToNbt(element, "minecraft:chat_type".equals(registryData.get("type").getAsString()));
            entries.add(new RegistryEntry(Key.key(name), nbt));
        }
        return entries;
    }

    private static NbtMap jsonToNbt(JsonObject json, boolean isChatType) {
        NbtMapBuilder builder = NbtMap.builder();

        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            String key = entry.getKey();
            JsonElement value = entry.getValue();

            if (isChatType && "parameters".equals(key) && value.isJsonArray()) {
                JsonArray array = value.getAsJsonArray();
                List<String> list = new ArrayList<>();
                for (JsonElement e : array) {
                    list.add(e.getAsString());
                }
                builder.putList(key, org.cloudburstmc.nbt.NbtType.STRING, list);
                continue;
            }

            if (value.isJsonArray()) {
                JsonArray array = value.getAsJsonArray();
                if (array.size() > 0 && array.get(0).isJsonPrimitive()) {
                    List<String> list = new ArrayList<>();
                    for (JsonElement e : array) list.add(e.getAsString());
                    builder.putList(key, org.cloudburstmc.nbt.NbtType.STRING, list);
                } else {
                    builder.putList(key, org.cloudburstmc.nbt.NbtType.COMPOUND, parseArrayAsCompound(array));
                }
            } else if (value.isJsonObject()) {
                builder.put(key, jsonToNbt(value.getAsJsonObject(), false));
            } else if (value.isJsonPrimitive()) {
                JsonPrimitive p = value.getAsJsonPrimitive();

                if (p.isBoolean()) {
                    builder.putByte(key, p.getAsBoolean() ? (byte) 1 : (byte) 0);
                } else if (p.isString()) {
                    builder.putString(key, p.getAsString());
                } else if (p.isNumber()) {
                    String str = p.getAsString();

                    // jukebox_song.length_in_seconds — Float
                    if (key.equals("length_in_seconds")) {
                        builder.putFloat(key, Float.parseFloat(str));
                        continue;
                    }

                    if (key.equals("offset") || key.equals("tick_chance")) {
                        builder.putDouble(key, Double.parseDouble(str));
                        continue;
                    }

                    // coordinate_scale — Double
                    if (key.equals("coordinate_scale")) {
                        builder.putDouble(key, Double.parseDouble(str));
                        continue;
                    }

                    // fixed_time — Long
                    if (key.equals("fixed_time")) {
                        builder.putLong(key, Long.parseLong(str));
                        continue;
                    }

                    // Всё остальное целое — Int
                    if (!str.contains(".")) {
                        builder.putInt(key, Integer.parseInt(str));
                    } else {
                        builder.putFloat(key, Float.parseFloat(str));
                    }
                }
            }
        }
        return builder.build();
    }

    private static List<NbtMap> parseArrayAsCompound(JsonArray array) {
        List<NbtMap> list = new ArrayList<>();
        for (JsonElement e : array) {
            if (e.isJsonObject()) {
                list.add(jsonToNbt(e.getAsJsonObject(), false));
            }
        }
        return list;
    }
}