package dev.minixr9k.handlers;

import dev.minixr9k.api.EventBus;
import dev.minixr9k.api.event.PlayerChatEvent;
import dev.minixr9k.api.event.PlayerCommandEvent;
import dev.minixr9k.api.event.PlayerJoinEvent;
import dev.minixr9k.api.event.PlayerQuitEvent;
import dev.minixr9k.config.Configuration;
import dev.minixr9k.config.PluginLoader;
import dev.minixr9k.features.World;
import dev.minixr9k.packets.beta.play.*;
import dev.minixr9k.packets.play.ClientboundKeepAlive;
import dev.minixr9k.registries.PacketBetaRegistry;
import dev.minixr9k.types.Inventory;
import dev.minixr9k.types.Player;
import dev.minixr9k.utils.BetaPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.concurrent.TimeUnit;

import static dev.minixr9k.features.World.sendChunks;

public class LegacyPlayState extends SimpleChannelInboundHandler<ByteBuf> {

    private final Player player;

    private long lastKeepAliveSentTime;
    private long lastKeepAliveId;
    private boolean keepAlivePending = false;

    private java.util.concurrent.ScheduledFuture<?> keepAliveTickFuture;
    private java.util.concurrent.ScheduledFuture<?> keepAliveTimeoutFuture;

    public LegacyPlayState(Player player) {
        this.player = player;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        int chunkRadius = Configuration.get().world.chunks;

        new SpawnPosition6Packet(0, 112, 0).send(ctx, player.getProtocolVersion());
        new UpdateHealth8Packet((short) 20).send(player.getCtx(), 14);
        player.teleport(0, 112, 0);

        sendChunks(ctx, 14, chunkRadius);

        World.spawnPlayers(player);
        World.addPlayer(player);

        EventBus.getInstance().callEvent(new PlayerJoinEvent(player));
        World.broadcast("§e{player} joined the game (id={entityId})".replace("{player}", player.getUsername()).replace("{entityId}", String.valueOf(player.getEntityId())));

        // keepalive
        startKeepAliveLoop(ctx);

        player.setLocalWorldTime(8000, false);
        player.setInventory(new Inventory(ctx, 14));
    }

    private void startKeepAliveLoop(ChannelHandlerContext ctx) {
        this.keepAliveTickFuture = ctx.executor().scheduleAtFixedRate(() -> {
            this.lastKeepAliveId = System.currentTimeMillis();
            new KeepAlive0Packet().send(ctx, 14);
            this.keepAlivePending = true;
            this.lastKeepAliveSentTime = System.currentTimeMillis();
        }, 5, 15, TimeUnit.SECONDS);

        this.keepAliveTimeoutFuture = ctx.executor().scheduleAtFixedRate(() -> {
            if (keepAlivePending && (System.currentTimeMillis() - lastKeepAliveSentTime > 30_000)) {
                this.keepAlivePending = false;
                System.out.println("[Play] KeepAlive Timeout!");
                ctx.close();
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    private void handleKeepAliveResponse(ByteBuf in) {
        if (in.readLong() == lastKeepAliveId) {
            this.keepAlivePending = false;
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        int packetId = in.readUnsignedByte();

        BetaPacket packet = PacketBetaRegistry.handle(packetId);

        if (packet != null) {
            packet.read(in, -1);
            handlePacket(ctx, packet);
        }
    }

    private void handlePacket(ChannelHandlerContext ctx, BetaPacket packet) {
        if (packet instanceof ChatMessage3Packet) {
            String message = ((ChatMessage3Packet) packet).getMessage();
            if (message.contains("§")) {
                player.kick("Illegal characters");
                return;
            }

            if (message.startsWith("/")) {
                String command = message.substring(1);

                EventBus.getInstance().callEvent(new PlayerCommandEvent(player, command));

                if (command.startsWith("spawn")) {
                    player.teleport(0, 112, 0);
                }

                if (command.startsWith("reload")) {
                    if (player.getOpLevel() < 4) {
                        player.sendMessage("Требуется 4 уровень оп для выполнения данной команды!");
                        return;
                    }
                    PluginLoader.disablePlugins();
                    PluginLoader.loadPlugins();
                    player.sendMessage("Плагины перезагружены!");
                }

                return;
            }

            EventBus.getInstance().callEvent(new PlayerChatEvent(player, message));
            World.broadcast("<username> ".replace("username", player.getUsername()) + message);
        }
        else if (packet instanceof PlayerPosition11Packet) {
            double x = ((PlayerPosition11Packet) packet).getX();
            double y = ((PlayerPosition11Packet) packet).getY();
            double z = ((PlayerPosition11Packet) packet).getZ();
            boolean onGround = ((PlayerPosition11Packet) packet).isOnGround();

            player.setX(x);
            player.setY(y);
            player.setZ(z);
            player.setOnGround(onGround);

            World.movePlayer(player);
        }
        else if (packet instanceof PlayerPositionAndLook13Packet) {
            double x = ((PlayerPositionAndLook13Packet) packet).getX();
            double y = ((PlayerPositionAndLook13Packet) packet).getY();
            double z = ((PlayerPositionAndLook13Packet) packet).getZ();
            float yaw = ((PlayerPositionAndLook13Packet) packet).getYaw();
            float pitch = ((PlayerPositionAndLook13Packet) packet).getPitch();
            boolean onGround = ((PlayerPositionAndLook13Packet) packet).isOnGround();

            player.setX(x);
            player.setY(y);
            player.setZ(z);
            player.setYaw(yaw);
            player.setPitch(pitch);
            player.setOnGround(onGround);

            World.movePlayer(player);
        }
        else if (packet instanceof PlayerLook12Packet) {
            float yaw = ((PlayerLook12Packet) packet).getYaw();
            float pitch = ((PlayerLook12Packet) packet).getPitch();
            boolean onGround = ((PlayerLook12Packet) packet).isOnGround();

            player.setYaw(yaw);
            player.setPitch(pitch);
            player.setOnGround(onGround);

            World.movePlayer(player);
        }
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
        World.broadcast("§e{player} quit the game".replace("{player}", player.getUsername()));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
    }
}