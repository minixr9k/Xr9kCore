package dev.minixr9k.packets.confAndPlay;

import dev.minixr9k.utils.MinecraftPacket;
import io.netty.buffer.ByteBuf;

import static dev.minixr9k.utils.ProtocolUtils.writeTextComponent;

public class ClientboundDisconnect implements MinecraftPacket {

    private final String reason;

    public ClientboundDisconnect(String reason) {
        this.reason = reason;
    }

    @Override
    public void write(ByteBuf out, int protocolVersion) {
        writeTextComponent(out, reason);
    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {

    }

    @Override
    public int getPacketId(int protocolVersion) {
        return 0x1C;
    }
}
