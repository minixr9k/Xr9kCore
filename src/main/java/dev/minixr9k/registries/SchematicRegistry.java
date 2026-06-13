package dev.minixr9k.registries;

import dev.minixr9k.utils.SchematicHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SchematicRegistry {

    private static final Map<Integer, String> fileRegistry = new HashMap<>();

    private static final Map<Integer, SchematicHandler> activeHandlers = new ConcurrentHashMap<>();

    public static void load() {
        System.out.println("[Registry] Registering block configuration paths...");

        fileRegistry.put(774, "blocks1.21.11.json");
        fileRegistry.put(773, "blocks1.21.10.json");
        fileRegistry.put(772, "blocks1.21.8.json");
        fileRegistry.put(771, "blocks1.21.6.json");
        fileRegistry.put(770, "blocks1.21.5.json");
        fileRegistry.put(769, "blocks1.21.4.json");
        fileRegistry.put(768, "blocks1.21.3.json");
        fileRegistry.put(767, "blocks1.21.1.json");

        System.out.println("[Registry] All paths registered.");
    }

    public static SchematicHandler getHandler(int protocolVersion) {
        int versionToLoad = fileRegistry.containsKey(protocolVersion) ? protocolVersion : 767;

        return activeHandlers.computeIfAbsent(versionToLoad, version -> {
            try {
                String path = fileRegistry.get(version);
                return new SchematicHandler(path);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });
    }
}