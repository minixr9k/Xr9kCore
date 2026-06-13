package dev.minixr9k.packets.play;

import dev.minixr9k.utils.MinecraftPacket;
import io.netty.buffer.ByteBuf;

public class ClientboundGameEvent implements MinecraftPacket {

    private final int eventId;
    private final float value;

    public ClientboundGameEvent(int eventId, float value) {
        this.eventId = eventId;
        this.value = value;
    }

    @Override
    public void write(ByteBuf out, int protocolVersion) {
        out.writeByte(eventId);
        out.writeFloat(value);
    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {

    }

    @Override
    public int getPacketId(int protocolVersion) {
        if (protocolVersion >= 773)
            return 0x26;
        else if (protocolVersion >= 770)
            return 0x22;
        else if (protocolVersion >= 768)
            return 0x23;
        else
            return 0x22;
    }
}
