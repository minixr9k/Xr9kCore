package dev.minixr9k.packets.play;

import dev.minixr9k.registries.ItemRegistry;
import dev.minixr9k.types.Inventory;
import dev.minixr9k.types.ItemStack;
import dev.minixr9k.utils.MinecraftPacket;
import io.netty.buffer.ByteBuf;

import static dev.minixr9k.utils.ProtocolUtils.writeVarInt;

public class ClientboundSetEquipmentList implements MinecraftPacket {

    private final int entityId;
    private final Inventory inventory;

    public ClientboundSetEquipmentList(int entityId, Inventory inventory) {
        this.entityId = entityId;
        this.inventory = inventory;
    }

    @Override
    public void write(ByteBuf out, int protocolVersion) {
        writeVarInt(out, entityId);

        for (int i = 0; i < 6; i++) {
            if (i == 1) continue;
            ItemStack itemStack = null;
            switch (i) {
                case 0 -> itemStack = inventory.getItemInMainHand();
                case 2 -> itemStack = inventory.getBoots();
                case 3 -> itemStack = inventory.getLeggings();
                case 4 -> itemStack = inventory.getChestplate();
                case 5 -> itemStack = inventory.getHelmet();
            }

            out.writeByte(i);

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
    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {

    }

    @Override
    public int getPacketId(int protocolVersion) {
        if (protocolVersion > 772)
            return 0x64;
        return 0x5F;
    }
}
