package dev.minixr9k.handlers;

import dev.minixr9k.packets.handshake.Handshake;
import dev.minixr9k.registries.PacketRegistry;
import dev.minixr9k.utils.MinecraftPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import static dev.minixr9k.utils.ProtocolUtils.readVarInt;

public class HandshakeState extends SimpleChannelInboundHandler<ByteBuf> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        if (!in.isReadable()) return;

        int packetId = readVarInt(in);

        MinecraftPacket packet = PacketRegistry.handleHandshake(packetId);

        if (packet != null) {
            packet.read(in, -1);

            handle(ctx, packet);
        }
    }

    private void handle(ChannelHandlerContext ctx, MinecraftPacket packet) {
        if (packet instanceof Handshake) {
            int protocolVersion = ((Handshake) packet).getProtocolVersion();
            String serverAddress = ((Handshake) packet).getServerAddress();
            int serverPort = ((Handshake) packet).getServerPort();
            int nextState = ((Handshake) packet).getNextState();

            System.out.printf("[Handshake] Protocol: %d, Address: %s:%d, NextState: %d%n",
                    protocolVersion, serverAddress, serverPort, nextState);

            switch (nextState) {
                case 1 -> {
                    ctx.pipeline().replace(this, "handler", new PingState(protocolVersion));
                    System.out.println("Switching to STATUS state");
                }
                case 2 -> {
                    ctx.pipeline().replace(this, "handler", new LoginState(protocolVersion));
                    System.out.println("Switching to LOGIN state");
                }
                default -> {
                    System.out.println("Unknown state requested: " + nextState);
                    ctx.close();
                }
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
    }
}