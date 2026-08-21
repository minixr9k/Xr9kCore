package dev.minixr9k.packets.play;

import dev.minixr9k.utils.MinecraftPacket;
import io.netty.buffer.ByteBuf;

import static dev.minixr9k.utils.ProtocolUtils.writeString;
import static dev.minixr9k.utils.ProtocolUtils.writeVarInt;

public class ClientboundSetCooldown implements MinecraftPacket {

    private final String item;
    private final int ticks;

    public ClientboundSetCooldown(String item, int ticks) {
        this.item = item;
        this.ticks = ticks;
    }

    @Override
    public void write(ByteBuf out, int protocolVersion) {
        writeString(out, item);
        writeVarInt(out, ticks);
    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {

    }

    @Override
    public int getPacketId(int protocolVersion) {
        return 0x16;
    }
}
