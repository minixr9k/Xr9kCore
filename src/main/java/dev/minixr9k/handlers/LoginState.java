package dev.minixr9k.handlers;

import dev.minixr9k.auth.PlayerProfile;
import dev.minixr9k.config.Configuration;
import dev.minixr9k.features.SkinCache;
import dev.minixr9k.features.World;
import dev.minixr9k.json.JConfig;
import dev.minixr9k.packets.login.ClientboundLoginDisconnect;
import dev.minixr9k.packets.login.ClientboundLoginSuccessPacket;
import dev.minixr9k.packets.login.ClientboundPluginRequest;
import dev.minixr9k.packets.login.serverbound.ServerboundLoginAck;
import dev.minixr9k.packets.login.serverbound.ServerboundLoginStart;
import dev.minixr9k.packets.login.serverbound.ServerboundPluginResponse;
import dev.minixr9k.registries.PacketRegistry;
import dev.minixr9k.types.Player;
import dev.minixr9k.utils.MinecraftPacket;
import dev.minixr9k.utils.ProtocolUtils;
import dev.minixr9k.utils.VelocityForwarding;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;

public class LoginState extends SimpleChannelInboundHandler<ByteBuf> {

    private final int protocolVersion;
    private final List<PlayerProfile> properties;
    private Player player;
    private int messageId;

    public LoginState(int protocolVersion, List<PlayerProfile> properties) {
        this.protocolVersion = protocolVersion;
        this.properties = properties;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        if (protocolVersion < 772) {
            System.out.println("[Xr9kCore] подключение с версией протокола " + protocolVersion);
            new ClientboundLoginDisconnect("Для подключения используйте версию 1.21.8").send(ctx, protocolVersion);
            ctx.close();
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        int packetId = ProtocolUtils.readVarInt(in);

        MinecraftPacket packet = PacketRegistry.handleLogin(packetId);

        if (packet != null) {
            packet.read(in, protocolVersion);
            handlePacket(ctx, packet, in);
        } else {
            ctx.close();
        }
    }

    private void handlePacket(ChannelHandlerContext ctx, MinecraftPacket packet, ByteBuf buf) throws NoSuchAlgorithmException, InvalidKeyException {
        if (packet instanceof ServerboundLoginStart) {

            String username = ((ServerboundLoginStart) packet).getUsername();
            UUID uuid = ((ServerboundLoginStart) packet).getUuid();

            if (username.length() < 3 || username.length() > 16) {
                new ClientboundLoginDisconnect("Слишком длинный либо короткий ник").send(ctx, protocolVersion);
                ctx.close();
            }

            if (World.getPlayer(username) != null) {
                new ClientboundLoginDisconnect("Игрок с таким ником уже играет на сервере!").send(ctx, protocolVersion);
                ctx.close();
            }

            if (Configuration.get().proxy.enabled && Configuration.get().proxy.forwardingMode == JConfig.ForwardingMode.MODERN) {

                ByteBuf payload = ctx.alloc().buffer();
                ProtocolUtils.writeVarInt(payload, 1);

                messageId = World.proxyMessageId.getAndIncrement();
                new ClientboundPluginRequest(messageId, "velocity:player_info", payload).send(ctx, protocolVersion);
                return;
            }

            player = new Player(ctx, protocolVersion);
            player.setUsername(username);
            player.setUuid(uuid);

            if (!properties.isEmpty()) {
                if (SkinCache.get(uuid) != null)
                    SkinCache.remove(uuid);
                SkinCache.put(uuid, properties);
            }

            System.out.println("[Xr9kCore/" + protocolVersion + "] UUID of player " + username + " is " + uuid);

            new ClientboundLoginSuccessPacket(uuid, username).send(ctx, protocolVersion);
        }
        else if (packet instanceof ServerboundPluginResponse) {
            int proxyMessageId = ((ServerboundPluginResponse) packet).getMessageId();
            boolean success = ((ServerboundPluginResponse) packet).isSuccessful();

            if (proxyMessageId == messageId) {
                if (!success || ((ServerboundPluginResponse) packet).getData() == null) {
                    new ClientboundLoginDisconnect("This server requires you to connect with Velocity.").send(ctx, protocolVersion);
                    ctx.close();
                    return;
                }
            }

            if (success) {
                try {
                    VelocityForwarding.ForwardedData forwardedData = VelocityForwarding.parse(((ServerboundPluginResponse) packet).getData(), Configuration.get().proxy.token);

                    UUID uuid = forwardedData.uuid;
                    String username = forwardedData.username;

                    player = new Player(ctx, protocolVersion);
                    player.setUuid(uuid);
                    player.setUsername(username);

                    if (!forwardedData.properties.isEmpty()) {
                        if (SkinCache.get(uuid) != null)
                            SkinCache.remove(uuid);
                        SkinCache.put(uuid, forwardedData.properties);
                    }

                    System.out.println("[Xr9kCore/Velocity] UUID of player " + player.getUsername() + " is " + player.getUuid());

                    new ClientboundLoginSuccessPacket(uuid, username).send(ctx, protocolVersion);
                } catch (Exception e) {
                    if (Configuration.get().features.debug)
                        System.err.println("Failed to verify Velocity payload: " + e.getMessage());
                    new ClientboundLoginDisconnect("Invalid proxy response").send(ctx, protocolVersion);
                    ctx.close();
                }
            }
        }
        else if (packet instanceof ServerboundLoginAck) {
            if (player == null) {
                ctx.close();
                return;
            }

            ctx.pipeline().replace(this, "config_handler", new ConfigurationState(protocolVersion, player));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
    }
}