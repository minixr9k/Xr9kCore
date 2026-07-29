package dev.minixr9k.packets.play;

import dev.minixr9k.types.WindowType;
import dev.minixr9k.utils.MinecraftPacket;
import io.netty.buffer.ByteBuf;

import static dev.minixr9k.utils.ProtocolUtils.writeTextComponent;
import static dev.minixr9k.utils.ProtocolUtils.writeVarInt;

public class ClientboundOpenScreen implements MinecraftPacket {

    private final int windowId;
    private final WindowType windowType;
    private final String windowTitle;

    public ClientboundOpenScreen(int windowId, WindowType windowType, String windowTitle) {
        this.windowId = windowId;
        this.windowType = windowType;
        this.windowTitle = windowTitle;
    }

    @Override
    public void write(ByteBuf out, int protocolVersion) {
        writeVarInt(out, windowId);
        writeVarInt(out, windowType.getId());
        writeTextComponent(out, windowTitle);
    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {

    }

    @Override
    public int getPacketId(int protocolVersion) {
        return 0x34;
    }
}
