package dev.minixr9k.handlers;

import dev.minixr9k.auth.PlayerProfile;
import dev.minixr9k.features.SkinCache;
import dev.minixr9k.features.World;
import dev.minixr9k.packets.beta.login.Login1Packet;
import dev.minixr9k.packets.beta.play.Disconnect255Packet;
import dev.minixr9k.packets.login.ClientboundLoginDisconnect;
import dev.minixr9k.packets.login.ClientboundLoginSuccessPacket;
import dev.minixr9k.packets.login.serverbound.ServerboundLoginAck;
import dev.minixr9k.packets.login.serverbound.ServerboundLoginStart;
import dev.minixr9k.registries.PacketBetaRegistry;
import dev.minixr9k.registries.PacketRegistry;
import dev.minixr9k.types.Player;
import dev.minixr9k.utils.BetaPacket;
import dev.minixr9k.utils.MinecraftPacket;
import dev.minixr9k.utils.ProtocolUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

public class LegacyLoginState extends SimpleChannelInboundHandler<ByteBuf> {

    private final String username;
    private Player player;

    public LegacyLoginState(String username) {
        this.username = username;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        int packetId = in.readUnsignedByte();

        BetaPacket packet = PacketBetaRegistry.handle(packetId);

        if (packet != null) {
            packet.read(in, -1);
            handlePacket(ctx, packet);
        } else {
            ctx.close();
        }
    }

    private void handlePacket(ChannelHandlerContext ctx, BetaPacket packet) {
        if (packet instanceof Login1Packet) {
            int protocolVersion = ((Login1Packet) packet).getProtocolVersion();
            String loginUsername = ((Login1Packet) packet).getUsername();

            if (!username.equals(loginUsername)) {
                ctx.close();
            }

            if (username.length() < 3 || username.length() > 16) {
                new Disconnect255Packet("Слишком длинный либо короткий ник").send(ctx, protocolVersion);
                ctx.close();
            }

            if (World.getPlayer(username) != null) {
                new Disconnect255Packet("Игрок с таким ником уже играет на сервере!").send(ctx, protocolVersion);
                ctx.close();
            }

            int entityId = World.globalEntityId.getAndIncrement();

            // ядро использует local ids и global ids, игроки всегда думают что у них айди 1
            new Login1Packet(1, (long)123L, (byte)0).send(ctx, protocolVersion);

            player = new Player(ctx, protocolVersion);
            player.setUsername(username);
            player.setUuid(UUID.nameUUIDFromBytes(username.getBytes(StandardCharsets.UTF_8)));
            player.setEntityId(entityId);

            System.out.println("beta player " + username + " -> play state");
            ctx.pipeline().replace(this, "legacyplay_handler", new LegacyPlayState(player));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
    }
}