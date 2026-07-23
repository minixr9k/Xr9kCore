package dev.minixr9k.packets.play;

import dev.minixr9k.utils.MinecraftPacket;
import io.netty.buffer.ByteBuf;

import static dev.minixr9k.utils.ProtocolUtils.writeVarInt;

public class ClientboundSetHealth implements MinecraftPacket {

    private final float health;
    private final int food;
    private final float saturation;

    public ClientboundSetHealth(float health, int food, float saturation) {
        this.health = health;
        this.food = food;
        this.saturation = saturation;
    }

    @Override
    public void write(ByteBuf out, int protocolVersion) {
        out.writeFloat(health);
        writeVarInt(out, food);
        out.writeFloat(saturation);
    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {

    }

    @Override
    public int getPacketId(int protocolVersion) {
        return 0x61;
    }
}
