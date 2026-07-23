package dev.minixr9k.packets.play;

import dev.minixr9k.utils.MinecraftPacket;
import io.netty.buffer.ByteBuf;

public class ClientboundPlayerAbilities implements MinecraftPacket {

    private byte flags;
    private float flyingSpeed;
    private float modifier;

    public ClientboundPlayerAbilities(byte flags, float flyingSpeed, float modifier) {
        this.flags = flags;
        this.flyingSpeed = flyingSpeed;
        this.modifier = modifier;
    }

    @Override
    public void write(ByteBuf out, int protocolVersion) {
        out.writeByte(flags);
        out.writeFloat(flyingSpeed);
        out.writeFloat(modifier);
    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {

    }

    @Override
    public int getPacketId(int protocolVersion) {
        if (protocolVersion == 773)
            return 0x40;
        else if (protocolVersion > 773)
            return 0x3E;
        return 0x39;
    }
}
