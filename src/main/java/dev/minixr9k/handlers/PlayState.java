package dev.minixr9k.handlers;

import dev.minixr9k.packets.play.*;
import dev.minixr9k.registries.SchematicRegistry;
import dev.minixr9k.utils.SchematicHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static dev.minixr9k.utils.ProtocolUtils.*;

public class PlayState extends SimpleChannelInboundHandler<ByteBuf> {

    private final int protocolVersion;

    private long lastKeepAliveSentTime;
    private long lastKeepAliveId;
    private boolean keepAlivePending = false;

    private final SchematicHandler schematicHandler;

    public PlayState(int protocolVersion) {
        this.protocolVersion = protocolVersion;
        this.schematicHandler = SchematicRegistry.getHandler(protocolVersion);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        new ClientboundJoinGame().send(ctx, protocolVersion);

        int chunkRadius = 8;

        for (int x = -chunkRadius; x <= chunkRadius; x++) {
            for (int z = -chunkRadius; z <= chunkRadius; z++) {

                new ClientboundChunkWithLight(x, z).send(ctx, protocolVersion);

            }
        }

        CompletableFuture.runAsync(() -> {
            try {
                File schemFile = new File("lobby.schem");

                if (!schemFile.exists()) {
                    System.err.println("[Play] 'lobby.schem' not found in server root!");
                    return;
                }

                java.util.Set<Long> affectedSections = new java.util.HashSet<>();

                schematicHandler.loadSchematic(schemFile.getName(), (x, y, z, id) -> {
                    int actualY = y + 100;
                    int chunkX = x >> 4;
                    int chunkZ = z >> 4;
                    int sectionY = (actualY - (-64)) / 16;

                    long sectionKey = ((long) chunkX & 0xFFFFFFL) << 40
                            | ((long) chunkZ & 0xFFFFFFL) << 16
                            | (sectionY & 0xFFFFL);
                    affectedSections.add(sectionKey);

                    ctx.executor().execute(() -> {
                        new ClientboundBlockUpdate(x, actualY, z, id).send(ctx, protocolVersion);
                    });
                });

                ctx.executor().execute(() -> {
                    // Отправляем свет
                    for (long key : affectedSections) {
                        int chunkX = (int) (key >> 40);
                        int chunkZ = (int) ((key >> 16) & 0xFFFFFFL);
                        int sectionY = (int) (key & 0xFFFFL);
                        int blockY = (sectionY * 16) - 64;

                        if ((chunkX & 0x800000) != 0) chunkX |= 0xFF000000;
                        if ((chunkZ & 0x800000) != 0) chunkZ |= 0xFF000000;

                        new ClientboundLightUpdate(chunkX, chunkZ, blockY).send(ctx, protocolVersion);
                    }

                    new ClientboundGameEvent(13, 0).send(ctx, protocolVersion);

                    if (protocolVersion > 767) {
                        new ClientboundSpawnEntity(100, UUID.randomUUID(), 11, 33.5, 104, 43.5, 180, 0, 180, 0, 0, 0, 0).send(ctx, protocolVersion);
                    } else {
                        new ClientboundSpawnEntity(100, UUID.randomUUID(), 7, 33.5, 104, 43.5, 180, 0, 180, 0, 0, 0, 0).send(ctx, protocolVersion);
                    }

                    new ClientboundSetEntityMetadata(100, "§6§lꜱᴇʀᴠᴇʀ", true).send(ctx, protocolVersion);

                    startKeepAliveLoop(ctx);

                    new ClientboundSyncPlayerPos(0, 33, 107, 31, 0, 0).send(ctx, protocolVersion);
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
                case 0x06 -> handleCommand(ctx, in);
                case 0x18 -> handleKeepAliveResponse(in);
                case 0x19 -> handleInteract(ctx, in);
                default -> in.skipBytes(in.readableBytes());
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
        ctx.executor().scheduleAtFixedRate(() -> {
            if (!ctx.channel().isActive()) return;

            this.lastKeepAliveId = System.currentTimeMillis();

            new ClientboundKeepAlive().send(ctx, protocolVersion);

            this.keepAlivePending = true;
            this.lastKeepAliveSentTime = System.currentTimeMillis();

        }, 5, 15, TimeUnit.SECONDS);

        ctx.executor().scheduleAtFixedRate(() -> {
            if (keepAlivePending && (System.currentTimeMillis() - lastKeepAliveSentTime > 30_000)) {
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

    private void handleTeleport(ByteBuf in) {
        int id = readVarInt(in);
        System.out.println("Teleport confirmed! id=" + id);
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

        in.readBoolean();

        if (entityId == 100)
            new ClientboundPluginMessage("bungeecord:main", "Connect", "auth").send(ctx, protocolVersion);

        System.out.println("Interact with entity=" + entityId + " type=" + type);
    }

    private void handleCommand(ChannelHandlerContext ctx, ByteBuf in) {
        int length = readVarInt(in);

        if (length > 64) {
            in.skipBytes(length);
            return;
        }

        String command = in.toString(in.readerIndex(), length, StandardCharsets.UTF_8);
        in.skipBytes(length);

        if (command.equalsIgnoreCase("lobby")) {
            System.out.println("Connecting to lobby...");
            new ClientboundPluginMessage("bungeecord:main", "Connect", "auth").send(ctx, protocolVersion);
        }
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}