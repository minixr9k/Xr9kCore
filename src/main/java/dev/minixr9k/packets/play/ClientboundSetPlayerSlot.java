package dev.minixr9k.packets.play;

import dev.minixr9k.registries.ComponentsRegistry;
import dev.minixr9k.registries.ItemRegistry;
import dev.minixr9k.types.ItemComponent;
import dev.minixr9k.types.ItemStack;
import dev.minixr9k.utils.MinecraftPacket;
import io.netty.buffer.ByteBuf;

import java.util.UUID;

import static dev.minixr9k.utils.ProtocolUtils.*;

public class ClientboundSetPlayerSlot implements MinecraftPacket {

    private final int slot;
    private final ItemStack itemStack;

    public ClientboundSetPlayerSlot(int slot, ItemStack itemStack) {
        this.slot = slot;
        this.itemStack = itemStack;
    }

    @Override
    public void write(ByteBuf out, int protocolVersion) {
        writeVarInt(out, slot); // item slot
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
    public void read(ByteBuf in, int protocolVersion) {

    }

    @Override
    public int getPacketId(int protocolVersion) {
        if (protocolVersion > 772)
            return 0x6A;
        return 0x65;
    }
}
