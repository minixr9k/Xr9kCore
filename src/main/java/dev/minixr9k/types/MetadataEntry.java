package dev.minixr9k.types;

import dev.minixr9k.utils.ProtocolUtils;
import io.netty.buffer.ByteBuf;

import static dev.minixr9k.utils.ProtocolUtils.writeVarInt;

public class MetadataEntry<T> {
    private final int index;
    private final Metadata type;
    private final T value;

    public MetadataEntry(int index, Metadata type, T value) {
        this.index = index;
        this.type = type;
        this.value = value;
    }

    public void write(ByteBuf out, int protocolVersion) {
        writeVarInt(out, index);
        writeVarInt(out, type.getId());

        switch (type) {
            case BYTE -> out.writeByte((Byte) value);
            case BOOLEAN -> out.writeBoolean((Boolean) value);
            case TEXT_COMPONENT -> ProtocolUtils.writeTextComponent(out, (String) value);
            case OPTIONAL_TEXT_COMPONENT -> {
                out.writeBoolean(value != null);
                if (value != null) ProtocolUtils.writeTextComponent(out, (String) value);
            }
            case POSE -> {
                writeVarInt(out, (Integer) value);
            }
        }
    }
}
