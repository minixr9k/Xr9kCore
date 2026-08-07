package dev.minixr9k.packets.play;

import dev.minixr9k.utils.MinecraftPacket;
import io.netty.buffer.ByteBuf;

import static dev.minixr9k.utils.ProtocolUtils.writeTextComponent;

public class ClientboundSetTitleAnimation implements MinecraftPacket {

    private final int fadeIn;
    private final int stay;
    private final int fadeOut;

    public ClientboundSetTitleAnimation(int fadeIn, int stay, int fadeOut) {
        this.fadeIn = fadeIn;
        this.stay = stay;
        this.fadeOut = fadeOut;
    }

    @Override
    public void write(ByteBuf out, int protocolVersion) {
        out.writeInt(fadeIn);
        out.writeInt(stay);
        out.writeInt(fadeOut);
    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {

    }

    @Override
    public int getPacketId(int protocolVersion) {
        return 0x6C;
    }
}
