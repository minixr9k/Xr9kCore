package dev.minixr9k.packets.play;

import dev.minixr9k.types.MetadataEntry;
import dev.minixr9k.utils.MinecraftPacket;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

import static dev.minixr9k.utils.ProtocolUtils.*;

public class ClientboundSetEntityMetadata implements MinecraftPacket {

    private final int entityId;
    private final List<MetadataEntry<?>> entries = new ArrayList<>();

    public ClientboundSetEntityMetadata(int entityId) {
        this.entityId = entityId;
    }

    public void add(MetadataEntry<?> entry) {
        entries.add(entry);
    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {}

    @Override
    public void write(ByteBuf out, int protocolVersion) {
        writeVarInt(out, entityId);

        for (MetadataEntry<?> entry : entries) {
            entry.write(out, protocolVersion);
        }

//        writeVarInt(out, 2);
//        writeVarInt(out, 6); // Serializer ID: Optional Component
//        out.writeBoolean(true); // Есть данные
//        writeTextComponent(out, customName);
//
//        // 3: Is Custom Name Visible (Boolean)
//        // "Boolean" имеет ID 8
//        writeVarInt(out, 3);
//        writeVarInt(out, 8); // Serializer ID: Boolean
//        out.writeBoolean(isNameVisible);

        // Конец метаданных
        out.writeByte(0xFF);
    }

    @Override
    public int getPacketId(int protocolVersion) {
        if (protocolVersion >= 773)
            return 0x61;

        if (protocolVersion >= 770)
            return 0x5C;

        if (protocolVersion <= 767)
            return 0x58;

        return 0x5D;
    }
}