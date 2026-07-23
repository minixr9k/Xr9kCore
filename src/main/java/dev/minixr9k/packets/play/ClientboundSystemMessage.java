package dev.minixr9k.packets.play;

import dev.minixr9k.utils.MinecraftPacket;
import dev.minixr9k.utils.ProtocolUtils;
import io.netty.buffer.ByteBuf;

public class ClientboundSystemMessage implements MinecraftPacket {

    private final String message;
    private final boolean overlay;

    public ClientboundSystemMessage(String message, boolean overlay) {
        this.message = message;
        this.overlay = overlay;
    }

    @Override
    public void write(ByteBuf out, int protocolVersion) {
        ProtocolUtils.writeTextComponent(out, message);
        out.writeBoolean(overlay);
    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {

    }

    @Override
    public int getPacketId(int protocolVersion) {
        if (protocolVersion <= 767)
            return 0x6C;
        else if (protocolVersion <= 772)
            return 0x72;
        else return 0x77;
    }
}
