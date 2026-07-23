package dev.minixr9k.packets.play;

import dev.minixr9k.utils.MinecraftPacket;
import io.netty.buffer.ByteBuf;

import static dev.minixr9k.utils.ProtocolUtils.writeVarInt;

public class ClientboundAckBlockChange implements MinecraftPacket {

    private final int sequence;

    public ClientboundAckBlockChange(int sequence) {
        this.sequence = sequence;
    }

    @Override
    public void write(ByteBuf out, int protocolVersion) {
        writeVarInt(out, sequence);
    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {

    }

    @Override
    public int getPacketId(int protocolVersion) {
        return 0x04;
    }
}
