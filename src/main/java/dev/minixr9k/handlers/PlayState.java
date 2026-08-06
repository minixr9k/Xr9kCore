package dev.minixr9k.handlers;

import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import dev.minixr9k.api.EventBus;
import dev.minixr9k.api.event.*;
import dev.minixr9k.auth.PlayerProfile;
import dev.minixr9k.config.Configuration;
import dev.minixr9k.config.PluginLoader;
import dev.minixr9k.features.SkinCache;
import dev.minixr9k.features.World;
import dev.minixr9k.packets.confAndPlay.ClientboundResourcepackPush;
import dev.minixr9k.packets.play.*;
import dev.minixr9k.registries.BlockRegistry;
import dev.minixr9k.types.*;
import dev.minixr9k.utils.Requests;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static dev.minixr9k.features.World.*;
import static dev.minixr9k.utils.ProtocolUtils.*;

public class PlayState extends SimpleChannelInboundHandler<ByteBuf> {

    private final int protocolVersion;
    private final Player player;

    private long lastKeepAliveSentTime;
    private long lastKeepAliveId;
    private boolean keepAlivePending = false;

    private final boolean anticheat = Configuration.get().anticheat;
    private double timerBalance = 10.0;
    private long lastPacketTime = System.currentTimeMillis();

    private final double MAX_TIMER_BUFFER = 10.0;
    private final double MIN_TIMER_BALANCE = 7.72;

    private java.util.concurrent.ScheduledFuture<?> keepAliveTickFuture;
    private java.util.concurrent.ScheduledFuture<?> keepAliveTimeoutFuture;

    public PlayState(int protocolVersion, Player player) {
        this.protocolVersion = protocolVersion;
        this.player = player;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        new ClientboundJoinGame(Configuration.get().gameMode, player).send(ctx, protocolVersion);

        int chunkRadius = Configuration.get().world.chunks;

        sendChunks(ctx, protocolVersion, chunkRadius);

        String resourcePack = Configuration.get().resourcepack.url;
        String resourcePackSha1 = Configuration.get().resourcepack.sha1;
        if (!resourcePack.isEmpty() && !resourcePackSha1.isEmpty()) {
            UUID uuid = UUID.nameUUIDFromBytes(resourcePack.getBytes(StandardCharsets.UTF_8));
            new ClientboundResourcepackPush(uuid, resourcePack, resourcePackSha1, Configuration.get().resourcepack.forced, Configuration.get().resourcepack.prompt).send(ctx, protocolVersion);
        }

        CompletableFuture.runAsync(() -> {
            try {
                ctx.executor().execute(() -> {
                    if (!ctx.channel().isActive()) {
                        return;
                    }

                    if (Configuration.get().buildInSkins && !SkinCache.has(player.getUuid())) {
                        List<PlayerProfile> skinData = Requests.getSkin(player.getUsername());
                        SkinCache.put(player.getUuid(), skinData);
                    }
                    player.setEntityId(World.globalEntityId.getAndIncrement());
                    World.spawnPlayers(player);
                    World.addPlayer(player);
                    World.spawnEntities(player);
                    EventBus.getInstance().callEvent(new PlayerJoinEvent(player));
                    if (Configuration.get().features.buildInMessages)
                        World.broadcast("§e{player} joined the game (id={entityId})".replace("{player}", player.getUsername()).replace("{entityId}", String.valueOf(player.getEntityId())));

                    startKeepAliveLoop(ctx);

                    if (protocolVersion <= 773)
                        player.setLocalWorldTime(Configuration.get().time, Configuration.get().isTimeIncreasing);
                    player.teleport(Configuration.get().spawnPosition);

                    int opLevel = Configuration.get().operators.getOrDefault(player.getUsername(), 0);
                    if (opLevel > 0) player.setOpLevel(opLevel);

                    player.sendGameEvent(13, 0);
//                    player.sendTabList("\n  (=^-ω-^=)   \n", "\n\n   §7RAM Usage: {usage}MB   \n§7Hosting: aeza.net".replace("{usage}", String.valueOf(getRssMemory())));
//                    player.getInventory().setItemHotbar(4, new ItemStack("minecraft:compass", (short) 1, List.of(new ItemComponent("minecraft:custom_name", "[{\"text\": \"Minigames\", \"color\":\"#FF7F50\"}]"))));
                    World.setEquipment(player);
                    player.updateInventory(0);
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf in) {
        if (!in.isReadable()) return;

        int packetId = readVarInt(in);

        // переделать

        if (protocolVersion >= 771)
            switch (packetId) {
                case 0x00 -> handleTeleport(in);
                case 0x04 -> handleChangeGamemode(in);
                case 0x06 -> handleCommand(ctx, in);
                case 0x08 -> handleChat(ctx, in);
                case 0x11 -> handleInventory(ctx, in);
                case 0x15 -> handlePlayerLoaded(ctx, in);
                case 0x1B -> handleKeepAliveResponse(in);
                case 0x19 -> handleInteract(ctx, in);
                case 0x1D -> handlePosition(ctx, in);
                case 0x1E -> handlePositionRotation(ctx, in);
                case 0x1F -> handleRotation(ctx, in);
                case 0x21 -> handleMoveVehicle(ctx, in);
                case 0x28 -> handlePlayerAction(ctx, in);
                case 0x29 -> handlePlayerCommand(ctx, in);
                case 0x2A -> handlePlayerInput(ctx, in);
                case 0x34 -> handleSetHotbarSlot(ctx, in);
                case 0x3C -> handleSwingArm(ctx, in);
                case 0x3F -> handleUseItemOn(ctx, in);
                case 0x40 -> handleUseItem(ctx, in);
                default -> {
//                    if (packetId != 0xC)
//                        System.out.println("Unknown packet: " + Integer.toHexString(packetId));
                    in.skipBytes(in.readableBytes());
                }
            }
        else if (protocolVersion == 767)
            switch (packetId) {
                case 0x00 -> handleTeleport(in);
                case 0x04 -> handleCommand(ctx, in);
                case 0x18 -> handleKeepAliveResponse(in);
                case 0x16 -> handleInteract(ctx, in);
                default -> in.skipBytes(in.readableBytes());
            }
        else
            switch (packetId) {
                case 0x00 -> handleTeleport(in);
                case 0x05 -> handleCommand(ctx, in);
                case 0x16 -> handleKeepAliveResponse(in);
                case 0x18 -> handleInteract(ctx, in);
                default -> in.skipBytes(in.readableBytes());
            }
    }

    private void startKeepAliveLoop(ChannelHandlerContext ctx) {
        this.keepAliveTickFuture = ctx.executor().scheduleAtFixedRate(() -> {
            this.lastKeepAliveId = System.currentTimeMillis();
            EventBus.getInstance().callEvent(new PlayerKeepAliveEvent(player));
            new ClientboundKeepAlive().send(ctx, protocolVersion);
//            player.sendTabList("\n  (=^-ω-^=)   \n", "\n\n   §7RAM Usage: {usage}MB   \n§7Hosting: aeza.net".replace("{usage}", String.valueOf(getRssMemory())));
            this.keepAlivePending = true;
            this.lastKeepAliveSentTime = System.currentTimeMillis();
        }, 5, 15, TimeUnit.SECONDS);

        this.keepAliveTimeoutFuture = ctx.executor().scheduleAtFixedRate(() -> {
            if (keepAlivePending && (System.currentTimeMillis() - lastKeepAliveSentTime > 30_000)) {
                this.keepAlivePending = false;
                System.out.println("[Xr9kCore/Play] " + player.getUsername() + " disconnected (keepalive timeout)");
                ctx.close();
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    private void handleKeepAliveResponse(ByteBuf in) {
        long timestamp = in.readLong();
        player.setPing((int) (System.currentTimeMillis() - timestamp));
        if (timestamp == lastKeepAliveId) {
            this.keepAlivePending = false;
        }
    }

    private void handleTeleport(ByteBuf in) {
        int id = readVarInt(in);
//        System.out.println("Teleport confirmed! id=" + id);
    }

    private void handleChangeGamemode(ByteBuf in) {
        int gamemode = readVarInt(in);
        if (player.getOpLevel() < 2) return;
        switch(gamemode) {
            case 0 -> player.setGamemode(GameMode.SURVIVAL);
            case 1 -> player.setGamemode(GameMode.CREATIVE);
            case 2 -> player.setGamemode(GameMode.ADVENTURE);
            case 3 -> player.setGamemode(GameMode.SPECTATOR);
        }
    }

    private void handleInteract(ChannelHandlerContext ctx, ByteBuf in) {
        int entityId = readVarInt(in);
        int type = readVarInt(in);

        if (type == 2) {
            in.readFloat();
            in.readFloat();
            in.readFloat();
        }

        if (type == 0 || type == 2)
            readVarInt(in);

        boolean isSneaking = in.readBoolean();

        EventBus.getInstance().callEvent(new PlayerInteractEntityEvent(player, entityId, type, isSneaking));

        if (type == 1) {
            Player p = World.getPlayer(entityId);
            if (p != null && (p.getGameMode() == GameMode.ADVENTURE || p.getGameMode() == GameMode.SURVIVAL)) {
                p.setHealth(p.getHealth() - 1);
                new ClientboundDamageEvent(entityId, 34).send(ctx, protocolVersion);
                return;
            }

            new ClientboundDamageEvent(entityId, 34).send(ctx, protocolVersion);
        }

//        System.out.println("Interact with entity=" + entityId + " type=" + type);
    }

    private void handlePosition(ChannelHandlerContext ctx, ByteBuf in) {
        double x = in.readDouble();
        double y = in.readDouble();
        double z = in.readDouble();
        byte flags = in.readByte();

        // timer detection
        long now = System.currentTimeMillis();
        long timeDiff = now - lastPacketTime;
        lastPacketTime = now;

        // Переводим прошедшее время в "игровые тики" (1 тик = 50 мс)
        double ticksEarned = timeDiff / 50.0;
        // Пополняем баланс, но ЖЕСТКО ограничиваем его сверху MAX_TIMER_BUFFER.
        // Если игрок стоял 2 секунды, ticksEarned будет 40, но запишется максимум 10.
        this.timerBalance = Math.min(MAX_TIMER_BUFFER, this.timerBalance + ticksEarned);
        this.timerBalance -= 1.0;

        // Если пакеты идут слишком быстро, timerBalance быстро уйдет в минус
        if (this.timerBalance < MIN_TIMER_BALANCE && anticheat) {
            player.teleportBack(player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch());
            this.timerBalance = MIN_TIMER_BALANCE + 1;
            return;
        }
        // timer detection -end-

        EventBus.getInstance().callEvent(new PlayerMoveEvent(player, x, y, z, player.getYaw(), player.getPitch()));

//        if (player.getTeleportImmunityTicks() > 0) {
//            player.decreaseImmunity();
//            player.setX(x);
//            player.setY(y);
//            player.setZ(z);
//            player.setOnGround(flags == 0x01);
//            World.movePlayer(player);
//            return;
//        }
//
//        double deltaY = y - player.getY();
//        // speed
//        double dx = x - player.getX();
//        double dz = z - player.getZ();
//        double distanceSq = dx * dx + dz * dz;
//        if (distanceSq > 15) {
//            player.teleportBack(player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch());
//            return;
//        }
//        // speed -end-
//
//        if (deltaY > 0.42 && !player.isFlying() && player.getGameMode() != GameMode.CREATIVE
//        && player.getGameMode() != GameMode.SPECTATOR && player.isLoaded() && !player.isOnGround()) {
//            player.teleportBack(player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch());
//            return;
//        }

        player.setX(x);
        player.setY(y);
        player.setZ(z);
        player.setOnGround(flags == 0x01);

        World.movePlayer(player);
    }

    private void handlePositionRotation(ChannelHandlerContext ctx, ByteBuf in) {
        double x = in.readDouble();
        double y = in.readDouble();
        double z = in.readDouble();
        float yaw = in.readFloat();
        float pitch = in.readFloat();
        byte flags = in.readByte();

        // timer detection
        long now = System.currentTimeMillis();
        long timeDiff = now - lastPacketTime;
        lastPacketTime = now;

        // Переводим прошедшее время в "игровые тики" (1 тик = 50 мс)
        double ticksEarned = timeDiff / 50.0;
        // Пополняем баланс, но ЖЕСТКО ограничиваем его сверху MAX_TIMER_BUFFER.
        // Если игрок стоял 2 секунды, ticksEarned будет 40, но запишется максимум 10.
        this.timerBalance = Math.min(MAX_TIMER_BUFFER, this.timerBalance + ticksEarned);
        this.timerBalance -= 1.0;

        // Если пакеты идут слишком быстро, timerBalance быстро уйдет в минус
        if (this.timerBalance < MIN_TIMER_BALANCE && anticheat) {
            player.teleportBack(player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch());
            this.timerBalance = MIN_TIMER_BALANCE + 1;
            return;
        }
        // timer detection -end-

        EventBus.getInstance().callEvent(new PlayerMoveEvent(player, x, y, z, yaw, pitch));

//        if (player.getTeleportImmunityTicks() > 0) {
//            player.decreaseImmunity();
//            player.setX(x);
//            player.setY(y);
//            player.setZ(z);
//            player.setYaw(yaw);
//            player.setPitch(pitch);
//            player.setOnGround(flags == 0x01);
//            World.movePlayer(player);
//            return;
//        }
//
//        double deltaY = y - player.getY();
//        // speed
//        double dx = x - player.getX();
//        double dz = z - player.getZ();
//        double distanceSq = dx * dx + dz * dz;
//        if (distanceSq > 15) {
//            player.teleportBack(player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch());
//            return;
//        }
//        // speed -end-
//
//        if (deltaY > 0.42 && !player.isFlying() && player.getGameMode() != GameMode.CREATIVE
//                && player.getGameMode() != GameMode.SPECTATOR && player.isLoaded() && !player.isOnGround()) {
//            player.teleportBack(player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch());
//            return;
//        }

        player.setX(x);
        player.setY(y);
        player.setZ(z);
        player.setYaw(yaw);
        player.setPitch(pitch);
        player.setOnGround(flags == 0x01);

        World.movePlayer(player);
    }

    private void handleRotation(ChannelHandlerContext ctx, ByteBuf in) {
        float yaw = in.readFloat();
        float pitch = in.readFloat();
        byte flags = in.readByte();

        EventBus.getInstance().callEvent(new PlayerMoveEvent(player, player.getX(), player.getY(), player.getZ(), yaw, pitch));

        player.setYaw(yaw);
        player.setPitch(pitch);
        player.setOnGround(flags == 0x01);

        World.movePlayer(player);
    }

    private void handleMoveVehicle(ChannelHandlerContext ctx, ByteBuf in) {
        double x = in.readDouble();
        double y = in.readDouble();
        double z = in.readDouble();
        float yaw = in.readFloat();
        float pitch = in.readFloat();
        boolean onGround = in.readBoolean();

        int entityType = player.getPassengerOfEntity();
        if (entityType != 0) {
            Entity entity = World.getEntity(entityType);
            if (entity == null) return;

            double deltaY = y - entity.getY();

            entity.updatePrevPosition();

            entity.setX(x);
            entity.setY(y);
            entity.setZ(z);
            entity.setYaw(yaw);
            entity.setPitch(pitch);
            player.setX(x);
            player.setY(y);
            player.setZ(z);

            if (deltaY > 0 && Configuration.get().features.fixBoatFly) {
                entity.setX(entity.getPrevX());
                entity.setY(entity.getPrevY());
                entity.setZ(entity.getPrevZ());
                new ClientboundSetPassengers(player.getPassengerOfEntity(), 0).send(ctx, protocolVersion);
                World.setPassenger(player, 0);
                player.setPassengerOfEntity(0);
            }

            World.moveEntity(entity);
        }
    }

    private void handlePlayerAction(ChannelHandlerContext ctx, ByteBuf in) {
        int status = readVarInt(in);
        long location = in.readLong();
        byte face = in.readByte();
        int sequence = readVarInt(in);

        int x = (int) (location >> 38);
        int y = (int) ((location << 52) >> 52);
        int z = (int) ((location << 26) >> 38);

        if ((status == 0 && player.getGameMode() == GameMode.CREATIVE)
                || (status == 2 && player.getGameMode() == GameMode.SURVIVAL)) {
            EventBus.getInstance().callEvent(new BlockBreakEvent(player, new Block(World.getBlock(x, y, z), new Location(x, y, z))));
            breakBlock(x, y, z);
            new ClientboundAckBlockChange(sequence).send(ctx, protocolVersion);
        }
        else if (status == 3) {
            EventBus.getInstance().callEvent(new PlayerInputEvent(player, InputKey.KEY_CTRL_Q));
            player.updateInventory(0);
        }
        else if (status == 4) {
            EventBus.getInstance().callEvent(new PlayerInputEvent(player, InputKey.KEY_Q));
            player.updateInventory(0);
        }
        else if (status == 6) {
            EventBus.getInstance().callEvent(new PlayerInputEvent(player, InputKey.KEY_F));
        }

//        System.out.println("status=" + status);
    }

    private void handlePlayerCommand(ChannelHandlerContext ctx, ByteBuf in) {
        int entityId = readVarInt(in);
        int actionId = readVarInt(in);
        int jumpBoost = readVarInt(in);

        switch (actionId) {
            case 1 -> { // start sprinting
                player.setSprinting(true);
            }
            case 2 -> { // stop sprinting
                player.setSprinting(false);
            }
            case 6 -> { // start flying with elytra
                player.setGliding(true);
            }
        }

//        System.out.println("actionId=" + actionId);
    }

    private void handlePlayerInput(ChannelHandlerContext ctx, ByteBuf in) {
        byte flags = in.readByte();

        EventBus.getInstance().callEvent(new PlayerInputEvent(player, flags));

        if (isPressed(InputKey.KEY_SHIFT, flags)) {
            player.setSneaking(true);
            sneak(player, true);
        }
        else {
            player.setSneaking(false);
            sneak(player, false);
        }
    }

    public boolean isPressed(InputKey key, byte flags) {
        return switch (key) {
            case KEY_W     -> (flags & 0x01) != 0;
            case KEY_A     -> (flags & 0x04) != 0;
            case KEY_S     -> (flags & 0x02) != 0;
            case KEY_D     -> (flags & 0x08) != 0;
            case KEY_SPACE -> (flags & 0x10) != 0;
            case KEY_SHIFT -> (flags & 0x20) != 0;
            case KEY_CTRL  -> (flags & 0x40) != 0;
            default        -> false;
        };
    }

    private void handleSetHotbarSlot(ChannelHandlerContext ctx, ByteBuf in) {
        short slot = in.readShort();
        if (slot < 0 || slot > 8) return;

        EventBus.getInstance().callEvent(new PlayerSetHotbarSlotEvent(player, slot));

        player.getInventory().setActiveSlot(slot);
        World.setEquipment(player);
    }

    private void handleSwingArm(ChannelHandlerContext ctx, ByteBuf in) {
        int hand = readVarInt(in);

        EventBus.getInstance().callEvent(new PlayerSwingEvent(player, hand));

        if (hand == 0) {
            swing(player, 0);
            return;
        }
        swing(player, 3);
    }

    private void handleUseItem(ChannelHandlerContext ctx, ByteBuf in) {
        int hand = readVarInt(in);
        int sequence = readVarInt(in);
        float yaw = in.readFloat();
        float pitch = in.readFloat();

        // right click air
        if (player.getInventory().getItemInMainHand().getType().equals("minecraft:fishing_rod")) {
            new ClientboundSpawnEntity(globalEntityId.getAndIncrement(), UUID.randomUUID(), "minecraft:fishing_bobber", player.getX(), player.getY(), player.getZ(), 0, 0, 0, 1, 0, 0, 0).send(ctx, protocolVersion);
        }
        EventBus.getInstance().callEvent(new PlayerInteractEvent(player, ActionType.RIGHT_CLICK_AIR));
    }

    private void handleUseItemOn(ChannelHandlerContext ctx, ByteBuf in) {
        int hand = readVarInt(in);
        long location = in.readLong();
        int face = readVarInt(in);
        float cursorPosX = in.readFloat();
        float cursorPosY = in.readFloat();
        float cursorPosZ = in.readFloat();
        boolean insideBlock = in.readBoolean();
        boolean worldBorderHit = in.readBoolean();
        int sequence = readVarInt(in);

        if (player.getInventory().getItemInMainHand() == null) {
            new ClientboundAckBlockChange(sequence).send(ctx, protocolVersion);
            return;
        }

        // right click block

        int x = (int) (location >> 38);
        int y = (int) ((location << 52) >> 52);
        int z = (int) ((location << 26) >> 38);

        EventBus.getInstance().callEvent(new PlayerInteractEvent(player, ActionType.RIGHT_CLICK_BLOCK, x, y, z));

        switch (face) {
            case 0 -> y--; // Кликнули снизу блока -> новый блок под ним
            case 1 -> y++; // Кликнули сверху блока -> новый блок над ним
            case 2 -> z--; // Кликнули по северной грани -> блок к северу (-Z)
            case 3 -> z++; // Кликнули по южной грани -> блок к югу (+Z)
            case 4 -> x--; // Кликнули по западной грани -> блок к западу (-X)
            case 5 -> x++; // Кликнули по восточной грани -> блок к востоку (+X)
        }

        if (x == Math.floor(player.getX()) && z == Math.floor(player.getZ()) && (y == Math.floor(player.getY()) || y == Math.floor(player.getY() + 1))) return;
        if (player.getGameMode() == GameMode.ADVENTURE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (player.getInventory().getItemInMainHand() == null) return;

        int blockId = BlockRegistry.getBlock(player.getInventory().getItemInMainHand().getType(), 772);
        if (blockId == -1) return;

        EventBus.getInstance().callEvent(new BlockPlaceEvent(player, new Block(World.getBlock(x, y, z), new Location(x, y, z))));

        placeBlock(x, y, z, blockId);
        new ClientboundAckBlockChange(sequence).send(ctx, protocolVersion);

        if (player.getGameMode() != GameMode.SURVIVAL) return;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null) return;
        if (item.getCount() - 1 > 0)
            player.getInventory().setItemHotbar(player.getInventory().getActiveSlot(), new ItemStack(item.getType(), (short) (item.getCount() - 1)));
        else
            player.getInventory().setItem(36 + player.getInventory().getActiveSlot(), null);
    }

    private void handlePlayerLoaded(ChannelHandlerContext ctx, ByteBuf in) {
        EventBus.getInstance().callEvent(new PlayerLoadedEvent(player));
        player.setLoaded(true);
    }

    private void handleCommand(ChannelHandlerContext ctx, ByteBuf in) {
        int length = readVarInt(in);

        if (length > 256) {
            in.skipBytes(length);
            return;
        }

        String command = in.toString(in.readerIndex(), length, StandardCharsets.UTF_8);
        in.skipBytes(length);

        EventBus.getInstance().callEvent(new PlayerCommandEvent(player, command));

        if (!Configuration.get().features.buildInCommands) return;

        if (command.startsWith("reload")) {
            if (player.getOpLevel() < 4) {
                player.sendMessage("Требуется 4 уровень оп для выполнения данной команды!");
                return;
            }
            PluginLoader.disablePlugins();
            PluginLoader.loadPlugins();
            player.sendMessage("Плагины перезагружены!");
        }

        if (command.startsWith("summon")) {
            if (player.getOpLevel() < 2) {
                player.sendMessage("Требуется 2 уровень оп для выполнения данной команды!");
                return;
            }
            if (command.length() < 8) return;
            String entity = command.substring(7);
            if (!entity.startsWith("minecraft:")) entity = "minecraft:" + entity;
            World.spawnEntityDev(player, entity);
        }

        if (command.startsWith("give")) {
            if (player.getOpLevel() < 2) {
                player.sendMessage("Требуется 2 уровень оп для выполнения данной команды!");
                return;
            }
            if (command.length() < 6) return;

            command = command.substring(5);
            int openIndex = command.indexOf("[");
            int closeIndex = command.indexOf("]");

            if (command.contains("[")) {

                String item = command.substring(0, openIndex);
                String components = command.substring(openIndex, closeIndex + 1);
                String countStr = command.substring(closeIndex + 1).trim();

                if (!item.startsWith("minecraft:")) item = "minecraft:" + item;

                if (countStr.isEmpty()) {
                    player.getInventory().setItemInMainHand(new ItemStack(item, (short) 1, parseComponents(components.trim())));
                }
                else {
                    try {
                        short count = Short.parseShort(countStr);
                        if (count < 1) return;
                        player.getInventory().setItemInMainHand(new ItemStack(item, count, parseComponents(components.trim())));
                    } catch (Exception e) {
                        player.getInventory().setItemInMainHand(new ItemStack(item, (short) 1, parseComponents(components.trim())));
                    }
                }
                World.setEquipment(player);
                player.updateInventory(0);
            }
            else {
                String item = command.trim();
                if (!item.startsWith("minecraft:")) item = "minecraft:" + item;
                player.getInventory().setItemInMainHand(new ItemStack(item, (short) 64));
                World.setEquipment(player);
                player.updateInventory(0);
            }
        }

        if (command.startsWith("kill")) {
            if (player.getOpLevel() < 2) {
                player.sendMessage("Требуется 2 уровень оп для выполнения данной команды!");
                return;
            }
            if (command.length() < 6) return;
            try {
                int entityId = Integer.parseInt(command.substring(5));
                World.removeEntity(entityId);
            } catch (Exception e) {
                player.sendMessage("Введите айди сущности!");
            }
        }

        if (command.startsWith("gamemode")) {
            if (player.getOpLevel() < 2) {
                player.sendMessage("Требуется 2 уровень оп для выполнения данной команды!");
                return;
            }
            if (command.length() < 10) return;
            try {
                String[] args = command.split(" ");
                if (args.length > 2) {
                    Player target = World.getPlayer(args[2]);
                    if (target != null)
                        target.setGamemode(GameMode.valueOf(args[1].toUpperCase()));
                    else
                        player.sendMessage("Игрок не в сети!");
                    return;
                }
                player.setGamemode(GameMode.valueOf(args[1].toUpperCase()));
            } catch (IllegalArgumentException e) {
                player.sendMessage("Неизвестный режим игры!");
            }
        }

        if (command.startsWith("tp")) {
            if (player.getOpLevel() < 2) {
                player.sendMessage("Требуется 2 уровень оп для выполнения данной команды!");
                return;
            }
            if (command.length() < 3) return;
            try {
                String[] args = command.split(" ");
                if (args.length > 1) {
                    Player target = World.getPlayer(args[1]);
                    if (target != null)
                        player.teleport(target.getX(), target.getY(), target.getZ(), target.getYaw(), target.getPitch());
                    else
                        player.sendMessage("Игрок не в сети!");
                }
            } catch (IllegalArgumentException e) {
                player.sendMessage("Произошла ошибка " + e);
            }
        }

        if (command.startsWith("kick")) {
            if (player.getOpLevel() < 4) {
                player.sendMessage("Требуется 4 уровень оп для выполнения данной команды!");
                return;
            }
            if (command.length() < 6) return;
            Player target = World.getPlayer(command.substring(5));
            if (target != null) {
                target.kick("Kicked by operator");
            }
            else {
                player.sendMessage("Игрок не в сети!");
            }
        }

        if (command.startsWith("tellraw")) {
            if (player.getOpLevel() < 2) {
                player.sendMessage("Требуется 2 уровень оп для выполнения данной команды!");
                return;
            }
            if (command.length() < 9) return;
            String json = command.substring(8);
            try {
                if (!JsonParser.parseString(json).isJsonArray()) {
                    player.sendMessage("Not a JSON Array");
                    return;
                }

                new ClientboundSystemMessage(json, false).send(ctx, protocolVersion);
            } catch (JsonSyntaxException e) {
                player.sendMessage("tellraw requires SNBT format");
            }
        }

        if (command.equals("clear")) {
            if (player.getOpLevel() < 2) {
                player.sendMessage("Требуется 2 уровень оп для выполнения данной команды!");
                return;
            }
            player.getInventory().clear();
            player.getInventory().setCarriedItem(null);
            player.updateInventory(0);
        }

        if (command.equals("save-all")) {
            if (player.getOpLevel() < 4) {
                player.sendMessage("Требуется 4 уровень оп для выполнения данной команды!");
                return;
            }
            World.broadcast("[LinearChunkHolder] Сохранение мира...");
            World.saveWorld();
            World.broadcast("[LinearChunkHolder] Успешно сохранен!");
        }

        if (command.equals("tree")) {
            if (player.getOpLevel() < 2) {
                player.sendMessage("Требуется 2 уровень оп для выполнения данной команды!");
                return;
            }
            World.generateTree((int) player.getX(), (int) player.getY(), (int) player.getZ());
        }
    }

    private void handleChat(ChannelHandlerContext ctx, ByteBuf in) {
        String message = readString(in);

        if (message.length() > 256) {
            in.skipBytes(in.readableBytes());
            return;
        }

        in.skipBytes(in.readableBytes());

        if (message.contains("§")) {
            player.kick("Illegal characters");
            return;
        }

        PlayerChatEvent event = new PlayerChatEvent(player, message);
        EventBus.getInstance().callEvent(event);

        String finalMessage = event.getStyle();

        World.broadcast(finalMessage);
    }

    private void handleInventory(ChannelHandlerContext ctx, ByteBuf in) {
        int windowId = readVarInt(in);
        int stateId = readVarInt(in);
        short slot = in.readShort();
        byte button = in.readByte();
        int mode = readVarInt(in);
        in.skipBytes(in.readableBytes());

        if (mode == 0) {
            switch (button) {
                case 0 -> {
                    ItemStack itemStack = player.getInventory().getItem(slot);
                    ItemStack carriedItem = player.getInventory().getCarriedItem();

                    if (carriedItem == null) {
                        if (itemStack != null) { // берем в курсор
                            player.getInventory().setCarriedItem(itemStack);
                            player.getInventory().setItem(slot, null);
                        }
                    }
                    else {
                        if (itemStack == null) { // кладем в пустой слот
                            player.getInventory().setItem(slot, carriedItem);
                            player.getInventory().setCarriedItem(null);
                        }
                        else {
                            if (!itemStack.getType().equals(carriedItem.getType())) {
                                // меняем местами предметы если они не одинового материала (1 в слот, другой в курсор)
                                player.getInventory().setItem(slot, carriedItem);
                                player.getInventory().setCarriedItem(itemStack);
                            }
                            else {
                                if (itemStack.getCount() + carriedItem.getCount() <= 64) {
                                    // если сумма не больше 64 то все чики пуки
                                    ItemStack newItemStack = new ItemStack(itemStack.getType(), (short) (itemStack.getCount() + carriedItem.getCount()), itemStack.getComponents());
                                    player.getInventory().setItem(slot, newItemStack);
                                    player.getInventory().setCarriedItem(null);
                                }
                                else {
                                    // если сумма больше 64
                                    int count = itemStack.getCount() + carriedItem.getCount() - 64;
                                    ItemStack newItemStack = new ItemStack(itemStack.getType(), (short) 64);
                                    ItemStack newCarriedItem = new ItemStack(carriedItem.getType(), (short) count);
                                    player.getInventory().setItem(slot, newItemStack);
                                    player.getInventory().setCarriedItem(newCarriedItem);
                                }
                            }
                        }
                    }
                }
                case 1 -> {
                    ItemStack itemStack = player.getInventory().getItem(slot);
                    ItemStack carriedItem = player.getInventory().getCarriedItem();

                    if (carriedItem == null) {
                        if (itemStack != null) { // берем в курсор
                            if (itemStack.getCount() > 1) { // если больше 1 берем половину
                                ItemStack newItemStack = new ItemStack(itemStack.getType(), (short) (itemStack.getCount() / 2), itemStack.getComponents());
                                player.getInventory().setCarriedItem(new ItemStack(itemStack.getType(), (short) (itemStack.getCount() - newItemStack.getCount()), itemStack.getComponents()));
                                player.getInventory().setItem(slot, newItemStack);
                            }
                            else {
                                player.getInventory().setCarriedItem(itemStack);
                                player.getInventory().setItem(slot, null);
                            }
                        }
                    }
                    else {
                        if (itemStack != null) {
                            // кладем по 1 блоку в слот
                            if (itemStack.getType().equals(carriedItem.getType())) {
                                if (itemStack.getCount() + 1 <= 64) {
                                    ItemStack newItemStack = new ItemStack(itemStack.getType(), (short) (itemStack.getCount() + 1), itemStack.getComponents());
                                    if (carriedItem.getCount() - 1 <= 0)
                                        player.getInventory().setCarriedItem(null);
                                    else
                                        player.getInventory().setCarriedItem(new ItemStack(itemStack.getType(), (short) (carriedItem.getCount() - 1), itemStack.getComponents()));
                                    player.getInventory().setItem(slot, newItemStack);
                                }
                            }
                            else {
                                // меняем местами предметы если они не одинового материала (1 в слот, другой в курсор)
                                player.getInventory().setItem(slot, carriedItem);
                                player.getInventory().setCarriedItem(itemStack);
                            }
                        }
                        else {
                            // кладем 1 блок в новый слот с курсора
                            ItemStack newItemStack = new ItemStack(carriedItem.getType(), (short) 1, carriedItem.getComponents());
                            if (carriedItem.getCount() - 1 <= 0)
                                player.getInventory().setCarriedItem(null);
                            else
                                player.getInventory().setCarriedItem(new ItemStack(carriedItem.getType(), (short) (carriedItem.getCount() - 1), carriedItem.getComponents()));
                            player.getInventory().setItem(slot, newItemStack);
                        }
                    }
                }
            }
        }
        else if (mode == 4) {
            switch (button) {
                case 0 -> {
                    // q
                    System.out.println("q");
                }
                case 1 -> {
                    // ctrl + q
                    System.out.println("ctrl q");
                }
            }
        }

        player.updateInventory(stateId);
        World.setEquipment(player);

//        System.out.println("mode=" + mode + " slot=" + slot + " button=" + button);
    }

    private List<ItemComponent> parseComponents(String rawComponents) {
        String components = rawComponents.substring(1);
        components = components.substring(0, components.length() - 1);

        String[] componentsList = components.split(",");
        List<ItemComponent> allComponents = new ArrayList<>();
        for (String c : componentsList) {
            String[] splitted = c.split("=");
            String componentName = splitted[0].trim();
            if (!componentName.startsWith("minecraft:")) componentName = "minecraft:" + componentName;
            Object value = "";
            if (!splitted[1].equals("{}"))
                value = splitted[1].trim();

            System.out.println("name=" + componentName + " value=" + value);

            allComponents.add(new ItemComponent(componentName, value));
        }

        return allComponents;
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {

        EventBus.getInstance().callEvent(new PlayerQuitEvent(player));

        if (keepAliveTickFuture != null) {
            keepAliveTickFuture.cancel(true);
            keepAliveTickFuture = null;
        }
        if (keepAliveTimeoutFuture != null) {
            keepAliveTimeoutFuture.cancel(true);
            keepAliveTimeoutFuture = null;
        }

        World.removePlayer(player);
        if (Configuration.get().features.buildInMessages)
            World.broadcast("§e{player} quit the game".replace("{player}", player.getUsername()));
//        System.gc();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    private static long getRssMemory() {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"sh", "-c",
                    "ps -o rss= -p " + ProcessHandle.current().pid()});
            try (java.util.Scanner s = new java.util.Scanner(process.getInputStream())) {
                if (s.hasNext()) {
                    long rss = s.nextLong(); // в килобайтах
                    return rss / 1024; // в мегабайтах
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
}