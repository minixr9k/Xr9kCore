package dev.minixr9k.handlers;

import dev.minixr9k.packets.configuration.ClientboundFinishConfiguration;
import dev.minixr9k.packets.configuration.ClientboundKnownPacks;
import dev.minixr9k.packets.configuration.ClientboundPluginMessage;
import dev.minixr9k.packets.configuration.serverbound.ServerboundFinishConfiguration;
import dev.minixr9k.packets.configuration.serverbound.ServerboundKnownPacksResponse;
import dev.minixr9k.registries.PacketRegistry;
import dev.minixr9k.registries.RegistryLoader;
import dev.minixr9k.utils.MinecraftPacket;
import dev.minixr9k.utils.ProtocolUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftCodecHelper;
import org.geysermc.mcprotocollib.protocol.packet.configuration.clientbound.ClientboundRegistryDataPacket;

import java.util.Map;
import static dev.minixr9k.utils.ProtocolUtils.*;

public class ConfigurationState extends SimpleChannelInboundHandler<ByteBuf> {

    private final int protocolVersion;

    public ConfigurationState(int protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        System.out.println("[Config] Handler added. Starting sequence...");

        new ClientboundKnownPacks("minecraft", "core", "1.21").send(ctx, 1);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        if (!in.isReadable()) return;
        int packetId = ProtocolUtils.readVarInt(in);

        MinecraftPacket packet = PacketRegistry.handleConfiguration(packetId);

        if (packet != null) {
            packet.read(in, protocolVersion);

            handle(ctx, packet);
        }
    }

    private void handle(ChannelHandlerContext ctx, MinecraftPacket packet) {
        if (packet instanceof ServerboundFinishConfiguration) {
            System.out.println("[Config] Transitioning to PLAY state...");
            ctx.pipeline().replace(this, "handler", new PlayState(protocolVersion));
        }
        else if (packet instanceof ServerboundKnownPacksResponse) {
            System.out.println("[Config] Client synced packs. Sending ALL registries...");

            new ClientboundPluginMessage("minecraft:brand", "MiniCore").send(ctx, 1);
            sendRegistryData(ctx);
            new ClientboundFinishConfiguration().send(ctx, 1);
        }
    }

    private void sendRegistryData(ChannelHandlerContext ctx) {
        String registryFilename = "";
        if (protocolVersion >= 770)
            registryFilename = "/registries.json";
        else if (protocolVersion == 769)
            registryFilename = "/registries1.21.4.json";
        else if (protocolVersion == 768)
            registryFilename = "/registries1.21.3.json";
        else
            registryFilename = "/registries1.21.1.json";

        Map<String, ClientboundRegistryDataPacket> allPackets = RegistryLoader.loadAllRegistries(registryFilename);

        String[] mandatoryRegistries;

        if (protocolVersion >= 772) {
            mandatoryRegistries = new String[]{
                    "minecraft:chat_type",
                    "minecraft:painting_variant",
                    "minecraft:jukebox_song",
                    "minecraft:dimension_type",
                    "minecraft:damage_type",
                    "minecraft:worldgen/biome",
                    "minecraft:wolf_variant",
                    "minecraft:wolf_sound_variant",
                    "minecraft:cat_variant",
                    "minecraft:frog_variant",
                    "minecraft:chicken_variant",
                    "minecraft:pig_variant",
                    "minecraft:cow_variant",
                    "minecraft:zombie_nautilus_variant"
            };
        }
        else if (protocolVersion >= 768) {
            mandatoryRegistries = new String[]{
                    "minecraft:chat_type",
                    "minecraft:painting_variant",
                    "minecraft:jukebox_song_old",
                    "minecraft:dimension_type",
                    "minecraft:damage_type",
                    "minecraft:worldgen/biome",
                    "minecraft:wolf_variant",
                    "minecraft:wolf_sound_variant",
                    "minecraft:cat_variant",
                    "minecraft:frog_variant",
                    "minecraft:chicken_variant",
                    "minecraft:pig_variant",
                    "minecraft:cow_variant",
                    "minecraft:zombie_nautilus_variant"
            };
        }
        else {
            mandatoryRegistries = new String[]{
                    "minecraft:trim_pattern",
                    "minecraft:trim_material",
                    "minecraft:banner_pattern",
                    "minecraft:chat_type",
                    "minecraft:painting_variant",
                    "minecraft:jukebox_song",
                    "minecraft:dimension_type",
                    "minecraft:damage_type",
                    "minecraft:worldgen/biome",
                    "minecraft:wolf_variant",
                    "minecraft:wolf_sound_variant"
            };
        }

        for (String key : mandatoryRegistries) {
            ClientboundRegistryDataPacket packet = allPackets.get(key);
            if (packet != null) {
                ByteBuf pBuf = ctx.alloc().buffer();
                writeVarInt(pBuf, 0x07); // Registry Data ID
                packet.serialize(pBuf, new MinecraftCodecHelper());
                sendPacket(ctx, pBuf);
            }
        }
    }

    private void sendPacket(ChannelHandlerContext ctx, ByteBuf payload) {
        ByteBuf full = ctx.alloc().buffer();
        writeVarInt(full, payload.readableBytes());
        full.writeBytes(payload);
        ctx.writeAndFlush(full);
        payload.release();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}