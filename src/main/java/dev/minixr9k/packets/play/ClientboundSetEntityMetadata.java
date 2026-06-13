package dev.minixr9k.packets.play;

import dev.minixr9k.utils.MinecraftPacket;
import io.netty.buffer.ByteBuf;

import static dev.minixr9k.utils.ProtocolUtils.*;

public class ClientboundSetEntityMetadata implements MinecraftPacket {

    private final int entityId;
    private final String customName;
    private final boolean isNameVisible;

    public ClientboundSetEntityMetadata(int entityId, String customName, boolean isNameVisible) {
        this.entityId = entityId;
        this.customName = customName;
        this.isNameVisible = isNameVisible;
    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {}

    @Override
    public void write(ByteBuf out, int protocolVersion) {
        writeVarInt(out, entityId);

        // 2: Custom Name (Optional Component)
        // "Optional Component" имеет ID 6
        writeVarInt(out, 2);
        writeVarInt(out, 6); // Serializer ID: Optional Component
        out.writeBoolean(true); // Есть данные

        writeTextComponent(out, customName);

        // 3: Is Custom Name Visible (Boolean)
        // "Boolean" имеет ID 8
        writeVarInt(out, 3);
        writeVarInt(out, 8); // Serializer ID: Boolean
        out.writeBoolean(isNameVisible);

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