package dev.minixr9k.handlers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.minixr9k.packets.handshake.Handshake;
import dev.minixr9k.registries.PacketRegistry;
import dev.minixr9k.utils.MinecraftPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import static dev.minixr9k.utils.ProtocolUtils.readVarInt;

public class HandshakeState extends SimpleChannelInboundHandler<ByteBuf> {

    private final boolean bungeeguardSupport = false;
    private final String token = "ZJTiGkkyj2d8";

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        if (!in.isReadable()) return;

        int packetLength = in.readableBytes();

        if (packetLength > 256 && !bungeeguardSupport) {
            System.out.println("[Xr9kCore] Пакет слишком огромный! (Handshake)");
            ctx.close();
            return;
        }

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

//            System.out.printf("[Handshake] Protocol: %d, Address: %s:%d, NextState: %d%n",
//                    protocolVersion, serverAddress, serverPort, nextState);

            if (bungeeguardSupport) {
                int openIndex = serverAddress.indexOf("[");
                int closeIndex = serverAddress.indexOf("]");

                String json = serverAddress.substring(openIndex, closeIndex + 1);
                if (JsonParser.parseString(json).isJsonArray()) {
                    JsonArray jsonArray = JsonParser.parseString(json).getAsJsonArray();
                    for (JsonElement el : jsonArray) {
                        JsonObject obj = el.getAsJsonObject();
                        if (obj.has("name")) {
                            if (obj.get("name").getAsString().equals("bungeeguard-token")) {
                                if (obj.has("value")) {
                                    if (obj.get("value").getAsString().equals(token)) {
                                        System.out.println("yeah!");
                                    }
                                }
                            }
                        }
                    }
                }
            }

            switch (nextState) {
                case 1 -> {
                    ctx.pipeline().replace(this, "handler", new PingState(protocolVersion));
                }
                case 2, 3 -> {
                    ctx.pipeline().replace(this, "handler", new LoginState(protocolVersion));
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