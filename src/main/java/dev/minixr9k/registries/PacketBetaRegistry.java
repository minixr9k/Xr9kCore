package dev.minixr9k.registries;

import dev.minixr9k.packets.beta.handshake.Handshake2Packet;
import dev.minixr9k.packets.beta.login.Login1Packet;
import dev.minixr9k.packets.beta.play.*;
import dev.minixr9k.utils.BetaPacket;

import java.util.HashMap;
import java.util.Map;

public class PacketBetaRegistry {

    private static final Map<Integer, Class<? extends BetaPacket>> packets = new HashMap<>();

    static {
        packets.put(0x00, KeepAlive0Packet.class);
        packets.put(0x01, Login1Packet.class);
        packets.put(0x02, Handshake2Packet.class);
        packets.put(0x03, ChatMessage3Packet.class);
        packets.put(0x04, TimeUpdate4Packet.class);
        packets.put(0x06, SpawnPosition6Packet.class);
        packets.put(0x08, UpdateHealth8Packet.class);
        packets.put(0x0B, PlayerPosition11Packet.class);
        packets.put(0x0C, PlayerLook12Packet.class);
        packets.put(0x0D, PlayerPositionAndLook13Packet.class);
        packets.put(0x14, NamedEntitySpawn20Packet.class);
        packets.put(0x1D, DestroyEntity29Packet.class);
        packets.put(0x22, EntityTeleport34Packet.class);
        packets.put(0xFF, Disconnect255Packet.class);
    }

    public static BetaPacket handle(int packetId) throws Exception {
        Class<? extends BetaPacket> clazz = packets.get(packetId);
        if (clazz == null) return null;
        return clazz.getDeclaredConstructor().newInstance();
    }

}
