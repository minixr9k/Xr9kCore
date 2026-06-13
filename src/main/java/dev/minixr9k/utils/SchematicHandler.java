package dev.minixr9k.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.cloudburstmc.nbt.NBTInputStream;
import org.cloudburstmc.nbt.NbtMap;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public class SchematicHandler {

    private final JsonObject blocksJson;

    public SchematicHandler(String resourcePath) throws Exception {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new FileNotFoundException("Resource file not found inside JAR: " + resourcePath);
            }

            try (InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                this.blocksJson = JsonParser.parseReader(reader).getAsJsonObject();
            }
        }
    }

    public void loadSchematic(String path, SchematicCallback callback) throws Exception {
        try (InputStream is = new GZIPInputStream(new FileInputStream(path));
             NBTInputStream nbtIs = new NBTInputStream(new DataInputStream(is))) {

            NbtMap root = (NbtMap) nbtIs.readTag();
            NbtMap schem = root.getCompound("Schematic");

            short width = schem.getShort("Width");
            short height = schem.getShort("Height");
            short length = schem.getShort("Length");

            NbtMap blocksMap = schem.getCompound("Blocks");
            byte[] blockData = blocksMap.getByteArray("Data");

            NbtMap palette = blocksMap.getCompound("Palette");
            Map<Integer, Integer> localToGlobalId = new HashMap<>();

            System.out.println("[Schematic] Mapping palette...");
            for (String blockState : palette.keySet()) {
                int localId = palette.getInt(blockState);
                int globalId = findGlobalId(blockState);
                localToGlobalId.put(localId, globalId);
            }

            int index = 0;
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < length; z++) {
                    for (int x = 0; x < width; x++) {
                        if (index >= blockData.length) break;

                        int localId = blockData[index++] & 0xFF;

                        Integer globalId = localToGlobalId.get(localId);

                        if (globalId != null && globalId != 0) {
                            callback.onBlock(x, y, z, globalId);
                        }
                    }
                }
            }
        }
    }

    private int findGlobalId(String schematicState) {
        if (schematicState.contains("air")) return 0;

        String blockName = schematicState;
        Map<String, String> props = new HashMap<>();

        if (schematicState.contains("[")) {
            blockName = schematicState.substring(0, schematicState.indexOf("["));
            String rawProps = schematicState.substring(schematicState.indexOf("[") + 1, schematicState.indexOf("]"));
            for (String pair : rawProps.split(",")) {
                String[] kv = pair.split("=");
                if (kv.length == 2) props.put(kv[0], kv[1]);
            }
        }

        if (!blocksJson.has(blockName)) return 0;

        JsonArray states = blocksJson.get(blockName).getAsJsonObject().getAsJsonArray("states");

        for (JsonElement el : states) {
            JsonObject state = el.getAsJsonObject();
            if (state.has("properties")) {
                JsonObject sProps = state.getAsJsonObject("properties");
                if (matchProperties(sProps, props)) return state.get("id").getAsInt();
            } else if (props.isEmpty()) {
                return state.get("id").getAsInt();
            }
        }

        for (JsonElement el : states) {
            if (el.getAsJsonObject().has("default") && el.getAsJsonObject().get("default").getAsBoolean()) {
                return el.getAsJsonObject().get("id").getAsInt();
            }
        }
        return states.get(0).getAsJsonObject().get("id").getAsInt();
    }

    private boolean matchProperties(JsonObject stateProps, Map<String, String> targetProps) {
        if (stateProps.size() != targetProps.size()) return false;
        for (Map.Entry<String, String> entry : targetProps.entrySet()) {
            if (!stateProps.has(entry.getKey()) ||
                    !stateProps.get(entry.getKey()).getAsString().equals(entry.getValue())) return false;
        }
        return true;
    }

    public interface SchematicCallback {
        void onBlock(int x, int y, int z, int id);
    }
}