package dev.minixr9k.packets.play;

import dev.minixr9k.registries.ItemRegistry;
import dev.minixr9k.types.ItemStack;
import dev.minixr9k.utils.MinecraftPacket;
import io.netty.buffer.ByteBuf;

import static dev.minixr9k.utils.ProtocolUtils.writeVarInt;

public class ClientboundSetEquipment implements MinecraftPacket {

    private final int entityId;
    private final int slot;
    private final ItemStack itemStack;

    public ClientboundSetEquipment(int entityId, int slot, ItemStack itemStack) {
        this.entityId = entityId;
        this.slot = slot;
        this.itemStack = itemStack;
    }

    @Override
    public void write(ByteBuf out, int protocolVersion) {
        writeVarInt(out, entityId);
        out.writeByte(slot);

        if (itemStack == null || itemStack.getType() == null || itemStack.getType().isEmpty())
            out.writeBoolean(false);
        else {
            writeVarInt(out, itemStack.getCount()); // item count
            int item = ItemRegistry.getItem(itemStack.getType(), protocolVersion);
            writeVarInt(out, item); // item id
            out.writeBoolean(false);
            out.writeBoolean(false);
        }
    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {

    }

    @Override
    public int getPacketId(int protocolVersion) {
        return 0x5F;
    }
}
