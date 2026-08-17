package dev.minixr9k.packets.play;

import dev.minixr9k.utils.MinecraftPacket;
import io.netty.buffer.ByteBuf;

import static dev.minixr9k.utils.ProtocolUtils.*;

public class ClientboundShowDialog implements MinecraftPacket {
    @Override
    public void write(ByteBuf out, int protocolVersion) {

        writeVarInt(out, 0);

        out.writeByte(10); // tag compound

        out.writeByte(8); // tag string
        writeNBTString(out, "type");
        writeNBTString(out, "minecraft:notice");

        out.writeByte(8); // tag string
        writeNBTString(out, "title");
        writeNBTString(out, "Test");

        out.writeByte(0); // tag end
    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {

    }

    @Override
    public int getPacketId(int protocolVersion) {
        return 0x85;
    }
}
