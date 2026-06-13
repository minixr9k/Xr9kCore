package dev.minixr9k.handlers;

import dev.minixr9k.packets.login.ClientboundLoginSuccessPacket;
import dev.minixr9k.packets.login.serverbound.ServerboundLoginAck;
import dev.minixr9k.packets.login.serverbound.ServerboundLoginStart;
import dev.minixr9k.registries.PacketRegistry;
import dev.minixr9k.utils.MinecraftPacket;
import dev.minixr9k.utils.ProtocolUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.util.UUID;

public class LoginState extends SimpleChannelInboundHandler<ByteBuf> {

    private final int protocolVersion;

    public LoginState(int protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        int packetId = ProtocolUtils.readVarInt(in);

        MinecraftPacket packet = PacketRegistry.handleLogin(packetId);

        if (packet != null) {
            packet.read(in, protocolVersion);
            handlePacket(ctx, packet);
        }
    }

    private void handlePacket(ChannelHandlerContext ctx, MinecraftPacket packet) {
        if (packet instanceof ServerboundLoginStart) {

            String username = ((ServerboundLoginStart) packet).getUsername();
            UUID uuid = ((ServerboundLoginStart) packet).getUuid();

            System.out.println("[Login] Player " + username + " is joining...");

            new ClientboundLoginSuccessPacket(uuid, username).send(ctx, protocolVersion);
        }
        else if (packet instanceof ServerboundLoginAck) {
            System.out.println("[Login] Transitioning to Configuration state...");
            ctx.pipeline().replace(this, "config_handler", new ConfigurationState(protocolVersion));
        }
    }
}