package dev.minixr9k.features;

import dev.minixr9k.auth.PlayerProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SkinCache {
    private static final Map<UUID, List<PlayerProfile>> cache = new ConcurrentHashMap<>();

    public static void put(UUID uuid, List<PlayerProfile> profile) {
        if (profile == null) return;
        cache.put(uuid, profile);
    }

    public static List<PlayerProfile> get(UUID uuid) {
        return cache.getOrDefault(uuid, new ArrayList<>());
    }

    public static boolean has(UUID uuid) {
        return cache.containsKey(uuid);
    }

    public static void remove(UUID uuid) {
        cache.remove(uuid);
    }
}
