package dev.minixr9k.packets.play;

import dev.minixr9k.registries.ComponentsRegistry;
import dev.minixr9k.registries.ItemRegistry;
import dev.minixr9k.types.ItemComponent;
import dev.minixr9k.types.ItemStack;
import dev.minixr9k.utils.MinecraftPacket;
import io.netty.buffer.ByteBuf;

import static dev.minixr9k.utils.ProtocolUtils.*;

public class ClientboundContainerSetContent implements MinecraftPacket {
    private final int windowId;
    private final int stateId;
    private final ItemStack[] slots;
    private final ItemStack carriedItem;

    public ClientboundContainerSetContent(int windowId, int stateId, ItemStack[] slots, ItemStack carriedItem) {
        this.windowId = windowId;
        this.stateId = stateId;
        this.slots = slots;
        this.carriedItem = carriedItem;
    }

    @Override
    public void write(ByteBuf out, int protocolVersion) {
        writeVarInt(out, windowId);
        writeVarInt(out, stateId);

        // Prefixed Array
        writeVarInt(out, slots.length);
        for (ItemStack item : slots) {
            writeItemStack(out, item, protocolVersion);
        }

        // Carried Item
        writeItemStack(out, carriedItem, protocolVersion);
    }

    private void writeItemStack(ByteBuf out, ItemStack item, int protocolVersion) {
        if (item == null || item.getCount() <= 0 || "minecraft:air".equalsIgnoreCase(item.getType())) {
            out.writeBoolean(false); // Слот пустой
        } else {
            int itemId = ItemRegistry.getItem(item.getType(), protocolVersion);
            writeVarInt(out, item.getCount());
            writeVarInt(out, itemId);

            if (item.getComponents() != null && !item.getComponents().isEmpty()) {
                out.writeBoolean(true);

                // количество добавляемых компонентов
                writeVarInt(out, item.getComponents().size());
                for (ItemComponent component : item.getComponents()) {
                    // ID компонента
                    writeVarInt(out, ComponentsRegistry.getComponent(component.getComponentType(), protocolVersion));

                    encodeComponent(out, component);
                }
            }
            else {
                out.writeBoolean(false);
            }

            out.writeBoolean(false);
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

        if (component.getComponentType().equalsIgnoreCase("minecraft:custom_name")) {
            writeTextComponent(out, component.getValueAsString());
        }
    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {}

    @Override
    public int getPacketId(int protocolVersion) {
        return 0x12;
    }
}
