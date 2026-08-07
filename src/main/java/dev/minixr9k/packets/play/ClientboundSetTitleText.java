package dev.minixr9k.packets.play;

import dev.minixr9k.utils.MinecraftPacket;
import io.netty.buffer.ByteBuf;

import static dev.minixr9k.utils.ProtocolUtils.writeTextComponent;

public class ClientboundSetTitleText implements MinecraftPacket {

    private final String message;

    public ClientboundSetTitleText(String message) {
        this.message = message;
    }

    @Override
    public void write(ByteBuf out, int protocolVersion) {
        writeTextComponent(out, message);
    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {

    }

    @Override
    public int getPacketId(int protocolVersion) {
        return 0x6B;
    }
}
