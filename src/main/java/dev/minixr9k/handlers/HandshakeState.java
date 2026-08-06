package dev.minixr9k.handlers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.minixr9k.api.EventBus;
import dev.minixr9k.api.event.HandshakeEvent;
import dev.minixr9k.auth.PlayerProfile;
import dev.minixr9k.config.Configuration;
import dev.minixr9k.json.JConfig;
import dev.minixr9k.packets.beta.handshake.Handshake2Packet;
import dev.minixr9k.packets.handshake.Handshake;
import dev.minixr9k.packets.login.ClientboundLoginDisconnect;
import dev.minixr9k.registries.PacketBetaRegistry;
import dev.minixr9k.registries.PacketRegistry;
import dev.minixr9k.utils.BetaPacket;
import dev.minixr9k.utils.ClientType;
import dev.minixr9k.utils.MinecraftPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.ArrayList;
import java.util.List;

import static dev.minixr9k.utils.ProtocolUtils.readVarInt;

public class HandshakeState extends SimpleChannelInboundHandler<ByteBuf> {

    private final ClientType clientType;

    public HandshakeState(ClientType clientType) {
        this.clientType = clientType;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        if (!in.isReadable()) return;

        int packetLength = in.readableBytes();

        if (packetLength > 2048) {
            if (Configuration.get().features.debug)
                System.out.println("[Xr9kCore] Пакет ооооочень огромный! (Handshake)");
            ctx.close();
            return;
        }

        if (packetLength > 256 && !Configuration.get().proxy.enabled) {
            if (Configuration.get().features.debug)
                System.out.println("[Xr9kCore] Пакет слишком огромный! (Handshake)");
            ctx.close();
            return;
        }

        if (clientType == ClientType.MODERN) {
            int packetId = readVarInt(in);

            MinecraftPacket packet = PacketRegistry.handleHandshake(packetId);

            if (packet != null) {
                packet.read(in, -1);

                handle(ctx, packet);
            }
        }
        else {
            int packetId = in.readUnsignedByte();

            try {
                BetaPacket packet = PacketBetaRegistry.handle(packetId);

                if (packet != null) {
                    packet.read(in, -1);
                    handleBeta(ctx, packet);
                } else {
                    System.out.println("Неизвестный Beta-пакет: 0x" + Integer.toHexString(packetId));
                }
            } catch (Throwable t) {
                System.err.println("Ошибка при создании или чтении пакета 0x" + Integer.toHexString(packetId));
                t.printStackTrace();
            }
        }
    }

    private void handle(ChannelHandlerContext ctx, MinecraftPacket packet) {
        if (packet instanceof Handshake) {
            int protocolVersion = ((Handshake) packet).getProtocolVersion();
            String serverAddress = ((Handshake) packet).getServerAddress();
            int serverPort = ((Handshake) packet).getServerPort();
            int nextState = ((Handshake) packet).getNextState();

            EventBus.getInstance().callEvent(new HandshakeEvent(ctx, protocolVersion, serverAddress, serverPort, nextState));

            List<PlayerProfile> properties = new ArrayList<>();

            if (Configuration.get().proxy.enabled && Configuration.get().proxy.forwardingMode == JConfig.ForwardingMode.BUNGEEGUARD) {
                if (nextState == 2) {

                    boolean authorized = false;

                    int openIndex = serverAddress.indexOf("[");
                    int closeIndex = serverAddress.indexOf("]");

                    String json = "";
                    if (openIndex != -1 && closeIndex != -1)
                        json = serverAddress.substring(openIndex, closeIndex + 1);
                    if (JsonParser.parseString(json).isJsonArray()) {
                        JsonArray jsonArray = JsonParser.parseString(json).getAsJsonArray();
                        for (JsonElement el : jsonArray) {
                            JsonObject obj = el.getAsJsonObject();
                            if (obj.has("name")) {
                                if (obj.get("name").getAsString().equals("bungeeguard-token")) {
                                    if (obj.has("value")) {
                                        if (obj.get("value").getAsString().equals(Configuration.get().proxy.token)) {
                                            authorized = true;
                                        }
                                    }
                                }

                                if (obj.has("value") && obj.has("signature")) {
                                    properties.add(new PlayerProfile("textures", obj.get("value").getAsString(), obj.get("signature").getAsString()));
                                }
                            }
                        }
                    }

                    if (Configuration.get().proxy.token.isEmpty()) {
                        authorized = true;
                    }

                    if (!authorized) {
                        new ClientboundLoginDisconnect("This server requires you to connect with Proxy").send(ctx, protocolVersion);
                        ctx.close();
                    }
                }
            }

            switch (nextState) {
                case 1 -> {
                    ctx.pipeline().replace(this, "handler", new PingState(protocolVersion));
                }
                case 2 -> {
                    ctx.pipeline().replace(this, "handler", new LoginState(protocolVersion, properties));
                }
                case 3 -> {
                    if (Configuration.get().features.acceptTransfers)
                        ctx.pipeline().replace(this, "handler", new LoginState(protocolVersion, properties));
                    else {
                        new ClientboundLoginDisconnect("This server does not accept transfers").send(ctx, protocolVersion);
                        ctx.close();
                    }
                }
                default -> {
                    if (Configuration.get().features.debug)
                        System.out.println("Unknown state requested: " + nextState);
                    ctx.close();
                }
            }
        }
    }

    private void handleBeta(ChannelHandlerContext ctx, BetaPacket packet) {
        if (packet instanceof Handshake2Packet) {
            String username = ((Handshake2Packet) packet).getUsername();
            System.out.println("beta client with username=" + username);

            new Handshake2Packet("-").send(ctx, -1);

            ctx.pipeline().replace(this, "handler", new LegacyLoginState(username));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
    }
}