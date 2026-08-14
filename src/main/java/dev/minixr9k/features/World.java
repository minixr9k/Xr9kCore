package dev.minixr9k.features;

import dev.minixr9k.api.chunk.Chunk;
import dev.minixr9k.api.chunk.ZRegionChunkManager;
import dev.minixr9k.auth.PlayerProfile;
import dev.minixr9k.packets.beta.play.*;
import dev.minixr9k.packets.play.*;
import dev.minixr9k.registries.BlockRegistry;
import dev.minixr9k.types.*;
import io.netty.channel.ChannelHandlerContext;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class World {

    public static final ScheduledExecutorService CHUNK_STREAMER = Executors.newScheduledThreadPool(2);

    private static final List<Player> players = new CopyOnWriteArrayList<>();
    private static final List<Entity> entities = new CopyOnWriteArrayList<>();
    private static final Map<String, Chunk> chunks = new ConcurrentHashMap<>();
    public static final AtomicInteger globalEntityId = new AtomicInteger(105);
    public static final AtomicInteger proxyMessageId = new AtomicInteger(100);
    private static final File WORLD_DIR = new File("world/region");

    public static List<int[]> getChunkSpiral(int radius) {
        List<int[]> chunks = new ArrayList<>();
        int x = 0, z = 0;
        int dx = 0, dz = -1;

        int maxChunks = (radius * 2 + 1) * (radius * 2 + 1);

        for (int i = 0; i < maxChunks; i++) {
            if (-radius <= x && x <= radius && -radius <= z && z <= radius) {
                chunks.add(new int[]{x, z});
            }
            if (x == z || (x < 0 && x == -z) || (x > 0 && x == 1 - z)) {
                int temp = dx;
                dx = -dz;
                dz = temp;
            }
            x += dx;
            z += dz;
        }
        return chunks;
    }

    public static void sendChunks(ChannelHandlerContext ctx, int protocolVersion, int chunkRadius) {
        List<int[]> chunkCoordinates = getChunkSpiral(chunkRadius);

        AtomicInteger index = new AtomicInteger(0);
        int chunksPerBatch = 4;

        final ScheduledFuture<?>[] taskHolder = new ScheduledFuture<?>[1];

        taskHolder[0] = CHUNK_STREAMER.scheduleAtFixedRate(() -> {
            if (!ctx.channel().isActive()) {
                if (taskHolder[0] != null) {
                    taskHolder[0].cancel(false);
                }
                return;
            }

            int start = index.getAndAdd(chunksPerBatch);

            if (start >= chunkCoordinates.size()) {
                if (taskHolder[0] != null) {
                    taskHolder[0].cancel(false);
                }
                return;
            }

            int end = Math.min(start + chunksPerBatch, chunkCoordinates.size());

            ctx.executor().execute(() -> {
                for (int i = start; i < end; i++) {
                    int[] pos = chunkCoordinates.get(i);
                    int x = pos[0];
                    int z = pos[1];

                    Chunk chunk = World.getChunkAt(x, z);
                    if (chunk != null) {
                        if (protocolVersion < 99) {
                            new PreChunk32Packet(x, z, true).send(ctx, protocolVersion);
                            new MapChunk33Packet(chunk).send(ctx, protocolVersion);
                            continue;
                        }
                        new ClientboundLinearChunkWithLight(chunk).send(ctx, protocolVersion);
                        new ClientboundChunkLightUpdate(x, z).send(ctx, protocolVersion);
                    }
                }
            });

        }, 0, 50, TimeUnit.MILLISECONDS);
    }

    public static void addPlayer(Player player) {
        int actions = 0x01 | 0x04 | 0x08 | 0x10;

        players.add(player);

        for (Player p : players) {
            if (p.getProtocolVersion() < 99) {
                if (p.getCtx() == player.getCtx()) continue;

                new NamedEntitySpawn20Packet(player.getEntityId(), player.getUsername(), player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch(), (short)0).send(p.getCtx(), p.getProtocolVersion());
                continue;
            }

            List<PlayerProfile> profile = SkinCache.get(player.getUuid());
            new ClientboundPlayerInfoUpdate(actions, player.getUuid(), player.getUsername(), profile).send(p.getCtx(), p.getProtocolVersion());
            new ClientboundSpawnEntity(player.getEntityId(), player.getUuid(), "minecraft:player", 33, 107, 31, 0, 0, 0, 0, 0, 0, 0).send(p.getCtx(), p.getProtocolVersion());
            if (p.getCtx() == player.getCtx()) {
                ClientboundSetEntityMetadata metadata = new ClientboundSetEntityMetadata(1);
                metadata.add(new MetadataEntry<>(p.getProtocolVersion() <= 772 ? 17 : 16, Metadata.BYTE, (byte) 127));
                metadata.send(p.getCtx(), p.getProtocolVersion());
            }
            else {
                ClientboundSetEntityMetadata metadata = new ClientboundSetEntityMetadata(player.getEntityId());
                metadata.add(new MetadataEntry<>(p.getProtocolVersion() <= 772 ? 17 : 16, Metadata.BYTE, (byte) 127));
                metadata.send(p.getCtx(), p.getProtocolVersion());
            }
        }
    }

    public static void spawnEntityPlayer(int entityId, UUID uuid, String username, Location loc, int actions, List<PlayerProfile> profile, Player viewer) {

        new ClientboundPlayerInfoUpdate(actions, uuid, username, profile).send(viewer.getCtx(), viewer.getProtocolVersion());
        new ClientboundSpawnEntity(entityId, uuid, "minecraft:player", loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch(), loc.getYaw(), 0, 0, 0, 0).send(viewer.getCtx(), viewer.getProtocolVersion());

    }

    public static void removePlayer(Player player) {
        players.remove(player);
        for (Player p : players) {
            new ClientboundPlayerInfoRemove(player.getUuid()).send(p.getCtx(), p.getProtocolVersion());
            new ClientboundRemoveEntity(player.getEntityId()).send(p.getCtx(), p.getProtocolVersion());
        }
    }

    public static List<Player> getAllPlayers() {
        return players;
    }

    public static void spawnPlayers(Player player) {
        int actions = 0x01 | 0x04 | 0x08 | 0x10;

        if (player.getProtocolVersion() < 99) {
            for (Player p : players) {
                if (p.getCtx() == player.getCtx()) continue;
                new NamedEntitySpawn20Packet(p.getEntityId(), p.getUsername(), p.getX(), p.getY(), p.getZ(), p.getYaw(), p.getPitch(), (short)0).send(player.getCtx(), player.getProtocolVersion());
            }
            return;
        }

        for (Player p : players) {
            List<PlayerProfile> profile = SkinCache.get(p.getUuid());
            new ClientboundPlayerInfoUpdate(actions, p.getUuid(), p.getUsername(), profile).send(player.getCtx(), player.getProtocolVersion());
            new ClientboundSpawnEntity(p.getEntityId(), p.getUuid(), "minecraft:player", p.getX(), p.getY(), p.getZ(), p.getYaw(), p.getPitch(), 0, 0, 0, 0, 0).send(player.getCtx(), player.getProtocolVersion());
            ClientboundSetEntityMetadata metadata = new ClientboundSetEntityMetadata(p.getEntityId());
            metadata.add(new MetadataEntry<>(17, Metadata.BYTE, (byte)127));
            metadata.send(player.getCtx(), player.getProtocolVersion());
            if (p.getInventory() != null) {
                ItemStack item = p.getInventory().getItemInMainHand();
                if (item == null) {
                    item = new ItemStack(null, (short) 0);
                }
                new ClientboundSetEquipment(p.getEntityId(), 0, new ItemStack(item.getType(), item.getCount())).send(player.getCtx(), player.getProtocolVersion());
            }
        }
    }

    public static void spawnEntities(Player player) {
        for (Entity en : entities) {
            new ClientboundSpawnEntity(en.getEntityId(), en.getUuid(), en.getEntityType(), en.getX(), en.getY(), en.getZ(), en.getYaw(), en.getPitch(), en.getYaw(), 0, 0, 0, 0).send(player.getCtx(), player.getProtocolVersion());
        }
    }

    public static void broadcast(String message) {
        System.out.println("[In-game/Chat] " + message);
        for (Player player : players) {
            player.sendMessage(message);
        }
    }

    public static void movePlayer(Player player) {
        byte yawAngle = (byte) (player.getYaw() * 256.0F / 360.0F);
        for (Player p : players) {
            if (p.getCtx() == player.getCtx()) continue;
            if (p.getProtocolVersion() < 99) {
                new EntityTeleport34Packet(player.getEntityId(), player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch()).send(p.getCtx(), p.getProtocolVersion());
                continue;
            }
            new ClientboundEntityPositionSync(player.getEntityId(), player.getX(), player.getY(), player.getZ(), 0, 0, 0, player.getYaw(), player.getPitch(), true).send(p.getCtx(), p.getProtocolVersion());
            new ClientboundSetHeadRotation(player.getEntityId(), yawAngle).send(p.getCtx(), p.getProtocolVersion());
        }
    }

    public static void moveEntity(Entity entity) {
        byte yawAngle = (byte) (entity.getYaw() * 256.0F / 360.0F);
        for (Player p : players) {
            new ClientboundEntityPositionSync(entity.getEntityId(), entity.getX(), entity.getY(), entity.getZ(), 0, 0, 0, entity.getYaw(), entity.getPitch(), true).send(p.getCtx(), p.getProtocolVersion());
            new ClientboundSetHeadRotation(entity.getEntityId(), yawAngle).send(p.getCtx(), p.getProtocolVersion());
        }
    }

    public static void setPassenger(Player player, int entityId) {
        for (Player p : players) {
            if (p.getCtx() == player.getCtx()) continue;
            if (p.getEntityId() == player.getPassengerOfEntity())
                new ClientboundSetPassengers(1, entityId).send(p.getCtx(), p.getProtocolVersion());
            else
                new ClientboundSetPassengers(player.getPassengerOfEntity(), entityId).send(p.getCtx(), p.getProtocolVersion());
        }
    }

    public static void swing(Player player, int animation) {
        for (Player p : players) {
            if (p.getCtx() == player.getCtx()) continue;
            if (p.getProtocolVersion() < 99) {
                // TODO beta1.7.3 swing
                continue;
            }
            new ClientboundEntityAnimation(player.getEntityId(), (byte)animation).send(p.getCtx(), p.getProtocolVersion());
        }
    }

    public static void sneak(Player player, boolean isSneaking) {
        ClientboundSetEntityMetadata packet = new ClientboundSetEntityMetadata(player.getEntityId());
        if (isSneaking) {
            packet.add(new MetadataEntry<>(0, Metadata.BYTE, (byte) 0x02));
            packet.add(new MetadataEntry<>(6, Metadata.POSE, 5)); // sneaking
        }
        else {
            packet.add(new MetadataEntry<>(0, Metadata.BYTE, (byte) 0x00));
            packet.add(new MetadataEntry<>(6, Metadata.POSE, 0)); // standing
        }
        for (Player p : players) {
            if (p.getCtx() == player.getCtx()) continue;
            if (p.getProtocolVersion() < 99) continue;
            packet.send(p.getCtx(), p.getProtocolVersion());
        }
    }

    public static void breakBlock(int x, int y, int z) {

        int chunkX = x >> 4;
        int chunkZ = z >> 4;

        Chunk chunk = getChunkAt(chunkX, chunkZ);
        if (chunk != null) {
            chunk.setBlockAt(x, y, z, (short) 0);
        }

        for (Player p : players) {
            if (p.getProtocolVersion() < 99) {
                new BlockChange53Packet(x, y, z, 0).send(p.getCtx(), p.getProtocolVersion());
                continue;
            }
            new ClientboundBlockUpdate(x, y, z, 0).send(p.getCtx(), p.getProtocolVersion());
        }
    }

    public static String getBlock(int x, int y, int z) {
        int chunkX = x >> 4;
        int chunkZ = z >> 4;

        Chunk chunk = getChunkAt(chunkX, chunkZ);
        if (chunk != null) {
            int blockId = chunk.getBlockAt(x, y, z);
            return (String) BlockRegistry.getBlockName(blockId, 772);
        }
        return "minecraft:air";
    }


    public static void placeBlock(int x, int y, int z, String material) {
        int blockId = BlockRegistry.getBlock(material, 772);
        if (blockId == -1) return;

        int chunkX = x >> 4;
        int chunkZ = z >> 4;

        Chunk chunk = getChunkAt(chunkX, chunkZ);
        if (chunk != null) {
            chunk.setBlockAt(x, y, z, (short) blockId);
        }

        for (Player p : players) {
            if (p.getProtocolVersion() < 99) {
                new BlockChange53Packet(x, y, z, blockId).send(p.getCtx(), p.getProtocolVersion());
                continue;
            }
            new ClientboundBlockUpdate(x, y, z, blockId).send(p.getCtx(), p.getProtocolVersion());
        }
    }

    public static void placeBlock(int x, int y, int z, int blockId) {
        if (blockId == -1) return;

        int chunkX = x >> 4;
        int chunkZ = z >> 4;

        Chunk chunk = getChunkAt(chunkX, chunkZ);
        if (chunk != null) {
            chunk.setBlockAt(x, y, z, (short) blockId);
        }

        for (Player p : players) {
            if (p.getProtocolVersion() < 99) {
                new BlockChange53Packet(x, y, z, blockId).send(p.getCtx(), p.getProtocolVersion());
                continue;
            }
            new ClientboundBlockUpdate(x, y, z, blockId).send(p.getCtx(), p.getProtocolVersion());
        }
    }

    public static void setLocalBlock(int x, int y, int z, String blockType, Player p) {
        int blockId = BlockRegistry.getBlock(blockType, 772);
        if (blockId == -1) return;

        if (p.getProtocolVersion() < 99) {
            new BlockChange53Packet(x, y, z, blockId).send(p.getCtx(), p.getProtocolVersion());
            return;
        }
        new ClientboundBlockUpdate(x, y, z, blockId).send(p.getCtx(), p.getProtocolVersion());
    }

    public static void setEquipment(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null) {
            item = new ItemStack(null, (short) 0);
        }
        for (Player p : players) {
            if (p.getCtx() == player.getCtx()) continue;
            new ClientboundSetEquipment(player.getEntityId(), 0, new ItemStack(item.getType(), item.getCount())).send(p.getCtx(), p.getProtocolVersion());
        }
    }

    public static Entity spawnEntity(Player player, String entityType) {
        int entityId = World.globalEntityId.getAndIncrement();
        UUID entityUUID = UUID.randomUUID();
        Entity entity = new Entity();
        entity.setEntityId(entityId);
        entity.setEntityType(entityType);
        entity.setUuid(entityUUID);
        entity.setX(player.getX());
        entity.setY(player.getY());
        entity.setZ(player.getZ());
        entity.setYaw(180);
        entities.add(entity);
        for (Player p : players) {
            new ClientboundSpawnEntity(entityId, entityUUID, entityType, player.getX(), player.getY(), player.getZ(), 180, 0, 180, 0, 0, 0, 0).send(p.getCtx(), p.getProtocolVersion());
        }
        return entity;
    }

    public static Entity spawnEntity(String entityType, double x, double y, double z) {
        int entityId = World.globalEntityId.getAndIncrement();
        UUID entityUUID = UUID.randomUUID();
        Entity entity = new Entity();
        entity.setEntityId(entityId);
        entity.setEntityType(entityType);
        entity.setUuid(entityUUID);
        entity.setX(x);
        entity.setY(y);
        entity.setZ(z);
        entities.add(entity);
        for (Player p : players) {
            new ClientboundSpawnEntity(entityId, entityUUID, entityType, x, y, z, 0, 0, 180, 0, 0, 0, 0).send(p.getCtx(), p.getProtocolVersion());
        }
        return entity;
    }

    public static Entity spawnEntity(String entityType, double x, double y, double z, float yaw, float pitch) {
        int entityId = World.globalEntityId.getAndIncrement();
        UUID entityUUID = UUID.randomUUID();
        Entity entity = new Entity();
        entity.setEntityId(entityId);
        entity.setEntityType(entityType);
        entity.setUuid(entityUUID);
        entity.setX(x);
        entity.setY(y);
        entity.setZ(z);
        entity.setYaw(yaw);
        entity.setPitch(pitch);
        entities.add(entity);
        for (Player p : players) {
            new ClientboundSpawnEntity(entityId, entityUUID, entityType, x, y, z, yaw, pitch, yaw, 0, 0, 0, 0).send(p.getCtx(), p.getProtocolVersion());
        }
        return entity;
    }

    public static void generateTree(int x, int y, int z) {
        // oh
        for (int i = 0; i < 7; i++) {
            if (i < 5)
                placeBlock(x, y + i, z, 137);
            if (i == 3 || i == 4) {
                placeBlock(x + 1, y + i, z, 255);
                placeBlock(x + 1, y + i, z + 1, 255);
                placeBlock(x + 1, y + i, z + 2, 255);
                placeBlock(x + 2, y + i, z + 1, 255);
                placeBlock(x + 2, y + i, z, 255);
                placeBlock(x - 1, y + i, z, 255);
                placeBlock(x - 1, y + i, z - 1, 255);
                placeBlock(x - 2, y + i, z - 1, 255);
                placeBlock(x - 1, y + i, z - 2, 255);
                placeBlock(x - 2, y + i, z, 255);

                placeBlock(x, y + i, z + 1, 255);
                placeBlock(x - 1, y + i, z + 1, 255);
                placeBlock(x - 2, y + i, z + 1, 255);
                placeBlock(x, y + i, z + 2, 255);
                placeBlock(x, y + i, z - 1, 255);
                placeBlock(x + 1, y + i, z - 1, 255);
                placeBlock(x + 2, y + i, z - 1, 255);
                placeBlock(x, y + i, z - 2, 255);
                placeBlock(x - 1, y + i, z + 2, 255);
                placeBlock(x + 1, y + i, z - 2, 255);
            }
            if (i == 5 || i == 6) {
                placeBlock(x, y + i, z, 255);

                placeBlock(x + 1, y + i, z, 255);
                placeBlock(x - 1, y + i, z, 255);

                placeBlock(x, y + i, z + 1, 255);
                placeBlock(x, y + i, z - 1, 255);
            }
            if (i == 5) {
                placeBlock(x + 1, y + i, z + 1, 255);
                placeBlock(x - 1, y + i, z + 1, 255);

                placeBlock(x + 1, y + i, z - 1, 255);
                placeBlock(x - 1, y + i, z - 1, 255);
            }
        }
    }

    public static Entity spawnEntityDev(Player player, String entityType) {
        int entityId = World.globalEntityId.getAndIncrement();
        UUID entityUUID = UUID.randomUUID();
        Entity entity = new Entity();
        entity.setEntityId(entityId);
        entity.setEntityType(entityType);
        entity.setUuid(entityUUID);
        entity.setX(player.getX());
        entity.setY(player.getY());
        entity.setZ(player.getZ());
        entity.setYaw(180);
        entities.add(entity);
        for (Player p : players) {
            new ClientboundSpawnEntity(entityId, entityUUID, entityType, player.getX(), player.getY(), player.getZ(), 180, 0, 180, 0, 0, 0, 0).send(p.getCtx(), p.getProtocolVersion());
        }
        return entity;
    }

    public static Player findNearablePlayer(double x, double y, double z) {
        Player nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (Player p : players) {
            double dx = p.getX() - x;
            double dy = p.getY() - y;
            double dz = p.getZ() - z;

            double distanceSq =  dx * dx + dy * dy + dz * dz;

            if (distanceSq < nearestDistance) {
                nearestDistance = distanceSq;
                nearest = p;
            }
        }

        return nearest;
    }

    public static void generateWorld(int chunkRadius, int grassRadius) {
        for (int x = -chunkRadius; x <= chunkRadius; x++) {
            for (int z = -chunkRadius; z <= chunkRadius; z++) {
                boolean isEmpty = !(Math.abs(x) <= grassRadius && Math.abs(z) <= grassRadius);

                Chunk chunk = new Chunk(x, z, isEmpty);
                chunks.put(x + "," + z, chunk);
            }
        }
    }

    // Инициализация мира
    public static void initWorld(int chunkRadius, int grassRadius) {
        try {
            if (WORLD_DIR.exists() && WORLD_DIR.list().length > 0) {
                System.out.println("[ChunkHolder] Папка сохранений найдена. Загружаем чанки...");
                Map<String, Chunk> loaded = ZRegionChunkManager.loadWorld(WORLD_DIR);
                chunks.putAll(loaded);
            } else {
                System.out.println("[ChunkHolder] Сохранения не найдены. Генерируем новый мир...");
                generateWorld(chunkRadius, grassRadius);
                saveWorld(); // Сразу сохраним свежесгенерированный
            }
        } catch (IOException e) {
            System.err.println("[ChunkHolder] Ошибка при работе с файлами мира! Генерируем во временную память...");
            e.printStackTrace();
            generateWorld(chunkRadius, grassRadius);
        }
    }

    // Сохранить текущее состояние
    public static void saveWorld() {
        try {
            System.out.println("[ChunkHolder] Запуск сохранения чанков в формате ZRegion...");
            ZRegionChunkManager.saveWorld(chunks, WORLD_DIR);
            System.out.println("[ChunkHolder] Мир успешно сохранен!");
        } catch (IOException e) {
            System.err.println("[ChunkHolder] Критическая ошибка при сохранении мира!");
            e.printStackTrace();
        }
    }

    public static Chunk getChunkAt(int x, int z) {
        return chunks.get(x + "," + z);
    }

    public static Map<String, Chunk> getLoadedChunks() {
        return chunks;
    }

    public static void removeEntity(int entityId) {
        Entity entity = getEntity(entityId);
        entities.remove(entity);
        for (Player p : players) {
            if (p.getProtocolVersion() < 99) {
                new DestroyEntity29Packet(entityId).send(p.getCtx(), p.getProtocolVersion());
                continue;
            }
            new ClientboundRemoveEntity(entityId).send(p.getCtx(), p.getProtocolVersion());
        }
    }

    public static Player getPlayer(String username) {
        return players.stream()
                .filter(player -> player.getUsername().equalsIgnoreCase(username))
                .findFirst()
                .orElse(null);
    }

    public static Player getPlayer(int entityId) {
        return players.stream()
                .filter(player -> player.getEntityId() == entityId)
                .findFirst()
                .orElse(null);
    }

    public static Entity getEntity(int entityId) {
        return entities.stream()
                .filter(entity -> entity.getEntityId() == entityId)
                .findFirst()
                .orElse(null);
    }


}
