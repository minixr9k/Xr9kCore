package dev.minixr9k.packets.play;

import dev.minixr9k.utils.MinecraftPacket;
import io.netty.buffer.ByteBuf;

import static dev.minixr9k.utils.ProtocolUtils.writeTextComponent;

public class ClientboundTabList implements MinecraftPacket {

    private final String header;
    private final String footer;

    public ClientboundTabList(String header, String footer) {
        this.header = header;
        this.footer = footer;
    }

    @Override
    public void write(ByteBuf out, int protocolVersion) {
        writeTextComponent(out, header);
        writeTextComponent(out, footer);
    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {

    }

    @Override
    public int getPacketId(int protocolVersion) {
        if (protocolVersion == 773)
            return 0x7A;
        else if (protocolVersion > 773)
            return 0x78;
        return 0x73;
    }
}
