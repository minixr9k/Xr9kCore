package dev.minixr9k.packets.play;

import dev.minixr9k.registries.SoundRegistry;
import dev.minixr9k.utils.MinecraftPacket;
import io.netty.buffer.ByteBuf;

import static dev.minixr9k.utils.ProtocolUtils.writeString;
import static dev.minixr9k.utils.ProtocolUtils.writeVarInt;

public class ClientboundStopSound implements MinecraftPacket {

    private final byte flags;
    private final int source;
    private final String sound;

    public ClientboundStopSound(byte flags, int source, String sound) {
        this.flags = flags;
        this.source = source;
        this.sound = sound;
    }

    @Override
    public void write(ByteBuf out, int protocolVersion) {
        out.writeByte(flags);
        if (flags == 1 || flags == 3)
            writeVarInt(out, source);

        if (flags == 2 || flags == 3)
            writeString(out, sound);
    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {

    }

    @Override
    public int getPacketId(int protocolVersion) {
        return 0x70;
    }
}
