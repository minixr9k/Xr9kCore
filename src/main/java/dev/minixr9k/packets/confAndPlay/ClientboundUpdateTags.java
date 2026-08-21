package dev.minixr9k.packets.confAndPlay;

import dev.minixr9k.utils.MinecraftPacket;
import io.netty.buffer.ByteBuf;

import java.util.List;

import static dev.minixr9k.utils.ProtocolUtils.writeString;
import static dev.minixr9k.utils.ProtocolUtils.writeVarInt;

public class ClientboundUpdateTags implements MinecraftPacket {

    private final String identifier;
    private final String tagName;
    private final List<Integer> entries;

    public ClientboundUpdateTags(String identifier, String tagName, List<Integer> entries) {
        this.identifier = identifier;
        this.tagName = tagName;
        this.entries = entries;
    }

    @Override
    public void write(ByteBuf out, int protocolVersion) {
        writeVarInt(out, 1);
        writeString(out, identifier);

        writeVarInt(out, 1);
        writeString(out, tagName);

        writeVarInt(out, entries.size());
        for (int entry : entries)
            writeVarInt(out,entry);
    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {

    }

    @Override
    public int getPacketId(int protocolVersion) {
        return 0x7F;
    }
}
