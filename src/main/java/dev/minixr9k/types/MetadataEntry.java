package dev.minixr9k.types;

import dev.minixr9k.registries.ComponentsRegistry;
import dev.minixr9k.registries.ItemRegistry;
import dev.minixr9k.utils.ProtocolUtils;
import io.netty.buffer.ByteBuf;

import static dev.minixr9k.utils.ProtocolUtils.writeString;
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
            case VAR_INT, BLOCK_STATE -> writeVarInt(out, (Integer) value);
            case FLOAT -> out.writeFloat((Float) value);
            case BOOLEAN -> out.writeBoolean((Boolean) value);
            case TEXT_COMPONENT -> ProtocolUtils.writeTextComponent(out, (String) value);
            case OPTIONAL_TEXT_COMPONENT -> {
                out.writeBoolean(value != null);
                if (value != null) ProtocolUtils.writeTextComponent(out, (String) value);
            }
            case SLOT -> {
                ItemStack itemStack = (ItemStack) value;

                writeVarInt(out, itemStack.getCount()); // item count
                int item = ItemRegistry.getItem(itemStack.getType(), protocolVersion);
                writeVarInt(out, item); // item id
                if (itemStack.getComponents() != null && !itemStack.getComponents().isEmpty()) {
                    out.writeBoolean(true);

                    // количество добавляемых компонентов
                    writeVarInt(out, itemStack.getComponents().size());
                    for (ItemComponent component : itemStack.getComponents()) {
                        // ID компонента
                        writeVarInt(out, ComponentsRegistry.getComponent(component.getComponentType(), protocolVersion));

                        encodeComponent(out, component);
                    }
                }
                else {
                    out.writeBoolean(false);
                }

                // Компоненты на удаление (optional)
                out.writeBoolean(false);
            }
            case POSE -> {
                writeVarInt(out, (Integer) value);
            }
            case ROTATIONS, VECTOR3F -> {
                Vector3f vec = (Vector3f) value;
                out.writeFloat(vec.x);
                out.writeFloat(vec.y);
                out.writeFloat(vec.z);
            }
            case QUATERNION -> {
                Vector4f vec = (Vector4f) value;
                out.writeFloat(vec.getX());
                out.writeFloat(vec.getY());
                out.writeFloat(vec.getZ());
                out.writeFloat(vec.getW());
            }
        }
    }

    private void encodeComponent(ByteBuf out, ItemComponent component) {
        if (component.getComponentType().equalsIgnoreCase("minecraft:custom_model_data")) { // custom_model_data
            writeVarInt(out, 1);
            out.writeFloat(component.getValueAsFloat());
            writeVarInt(out, 0);
            writeVarInt(out, 0);
            writeVarInt(out, 0);
        }

        if (component.getComponentType().equalsIgnoreCase("minecraft:profile")) { // profile
            out.writeBoolean(false); // optional username
            out.writeBoolean(false); // optional uuid

            writeVarInt(out, 1); // properties

            writeString(out, "textures");
            writeString(out, component.getValueAsString());
            out.writeBoolean(false); // signature empty
        }
    }
}
