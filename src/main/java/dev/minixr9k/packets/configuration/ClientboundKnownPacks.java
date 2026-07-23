package dev.minixr9k.packets.configuration;

import dev.minixr9k.utils.MinecraftPacket;
import io.netty.buffer.ByteBuf;

import static dev.minixr9k.utils.ProtocolUtils.writeString;
import static dev.minixr9k.utils.ProtocolUtils.writeVarInt;

public class ClientboundKnownPacks implements MinecraftPacket {

    private final String namespace;
    private final String id;
    private final String version;

    public ClientboundKnownPacks(String namespace, String id, String version) {
        this.namespace = namespace;
        this.id = id;
        this.version = version;
    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {}

    @Override
    public void write(ByteBuf out, int protocolVersion) {
        writeVarInt(out, 1);
        writeString(out, namespace);
        writeString(out, id);
        writeString(out, version);
    }

    @Override
    public int getPacketId(int protocolVersion) {
        return 0x0E;
    }
}
