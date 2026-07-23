package dev.minixr9k.packets.play;

import dev.minixr9k.registries.ComponentsRegistry;
import dev.minixr9k.registries.ItemRegistry;
import dev.minixr9k.types.ItemComponent;
import dev.minixr9k.types.ItemStack;
import dev.minixr9k.utils.MinecraftPacket;
import io.netty.buffer.ByteBuf;

import static dev.minixr9k.utils.ProtocolUtils.writeVarInt;

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

                if (component.getComponentValue() != -1) {
                    writeVarInt(out, 1);
                    out.writeFloat(component.getComponentValue());
                    writeVarInt(out, 0);
                    writeVarInt(out, 0);
                    writeVarInt(out, 0);
                }
            }
        }
        else {
            out.writeBoolean(false);
        }

        // Компоненты на удаление (optional)
        out.writeBoolean(false);
    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {

    }

    @Override
    public int getPacketId(int protocolVersion) {
        if (protocolVersion == 773)
            return 0x6C;
        else if (protocolVersion > 773)
            return 0x6A;
        return 0x65;
    }
}
