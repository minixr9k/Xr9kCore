package dev.minixr9k.handlers;

import dev.minixr9k.features.World;
import dev.minixr9k.packets.login.ClientboundLoginDisconnect;
import dev.minixr9k.packets.login.ClientboundLoginSuccessPacket;
import dev.minixr9k.packets.login.serverbound.ServerboundLoginAck;
import dev.minixr9k.packets.login.serverbound.ServerboundLoginStart;
import dev.minixr9k.registries.PacketRegistry;
import dev.minixr9k.types.Player;
import dev.minixr9k.utils.MinecraftPacket;
import dev.minixr9k.utils.ProtocolUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.util.UUID;

public class LoginState extends SimpleChannelInboundHandler<ByteBuf> {

    private final int protocolVersion;
    private Player player;

    public LoginState(int protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        if (protocolVersion < 772) {
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
            handlePacket(ctx, packet);
        } else {
            ctx.close();
        }
    }

    private void handlePacket(ChannelHandlerContext ctx, MinecraftPacket packet) {
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

            player = new Player();
            player.setProtocolVersion(protocolVersion);
            player.setCtx(ctx);
            player.setUsername(username);
            player.setUuid(uuid);

            System.out.println("[Xr9kCore] UUID of player " + username + " is " + uuid);

            new ClientboundLoginSuccessPacket(uuid, username).send(ctx, protocolVersion);
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