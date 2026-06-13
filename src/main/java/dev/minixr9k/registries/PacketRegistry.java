package dev.minixr9k.registries;

import dev.minixr9k.packets.configuration.serverbound.ServerboundFinishConfiguration;
import dev.minixr9k.packets.configuration.serverbound.ServerboundKnownPacksResponse;
import dev.minixr9k.packets.handshake.Handshake;
import dev.minixr9k.packets.login.serverbound.ServerboundLoginAck;
import dev.minixr9k.packets.login.serverbound.ServerboundLoginStart;
import dev.minixr9k.utils.MinecraftPacket;

import java.util.HashMap;
import java.util.Map;

public class PacketRegistry {

    private static final Map<Integer, Class<? extends MinecraftPacket>> handshakePackets = new HashMap<>();
    private static final Map<Integer, Class<? extends MinecraftPacket>> loginPackets = new HashMap<>();
    private static final Map<Integer, Class<? extends MinecraftPacket>> configurationPackets = new HashMap<>();

    static {
        handshakePackets.put(0x00, Handshake.class);

        loginPackets.put(0x00, ServerboundLoginStart.class);
        loginPackets.put(0x03, ServerboundLoginAck.class);

        configurationPackets.put(0x03, ServerboundFinishConfiguration.class);
        configurationPackets.put(0x07, ServerboundKnownPacksResponse.class);
    }

    public static MinecraftPacket handleHandshake(int packetId) throws Exception {
        Class<? extends MinecraftPacket> clazz = handshakePackets.get(packetId);
        if (clazz == null) return null;
        return clazz.getDeclaredConstructor().newInstance();
    }

    public static MinecraftPacket handleLogin(int packetId) throws Exception {
        Class<? extends MinecraftPacket> clazz = loginPackets.get(packetId);
        if (clazz == null) return null;
        return clazz.getDeclaredConstructor().newInstance();
    }

    public static MinecraftPacket handleConfiguration(int packetId) throws Exception {
        Class<? extends MinecraftPacket> clazz = configurationPackets.get(packetId);
        if (clazz == null) return null;
        return clazz.getDeclaredConstructor().newInstance();
    }

}
