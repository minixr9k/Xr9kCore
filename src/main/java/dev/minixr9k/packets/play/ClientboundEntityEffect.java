package dev.minixr9k.packets.play;

import dev.minixr9k.registries.EffectRegistry;
import dev.minixr9k.utils.MinecraftPacket;
import io.netty.buffer.ByteBuf;

import static dev.minixr9k.utils.ProtocolUtils.writeVarInt;

public class ClientboundEntityEffect implements MinecraftPacket {

    private final int entityId;
    private final String effect;
    private final int amplifier;
    private final int duration;
    private final byte flags;

    public ClientboundEntityEffect(int entityId, String effect, int amplifier, int duration, byte flags) {
        this.entityId = entityId;
        this.effect = effect;
        this.amplifier = amplifier;
        this.duration = duration;
        this.flags = flags;
    }

    @Override
    public void write(ByteBuf out, int protocolVersion) {
        int effectId = EffectRegistry.getEffect(effect, protocolVersion);
        writeVarInt(out, entityId);
        writeVarInt(out, effectId);
        writeVarInt(out, amplifier);
        writeVarInt(out, duration);
        out.writeByte(flags);
    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {

    }

    @Override
    public int getPacketId(int protocolVersion) {
        return 0x7D;
    }
}
