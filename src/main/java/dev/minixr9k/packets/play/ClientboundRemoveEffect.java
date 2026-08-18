package dev.minixr9k.packets.play;

import dev.minixr9k.registries.EffectRegistry;
import dev.minixr9k.utils.MinecraftPacket;
import io.netty.buffer.ByteBuf;

import static dev.minixr9k.utils.ProtocolUtils.writeVarInt;

public class ClientboundRemoveEffect implements MinecraftPacket {

    private final int entityId;
    private final String effect;

    public ClientboundRemoveEffect(int entityId, String effect) {
        this.entityId = entityId;
        this.effect = effect;
    }

    @Override
    public void write(ByteBuf out, int protocolVersion) {
        int effectId = EffectRegistry.getEffect(effect, protocolVersion);
        writeVarInt(out, entityId);
        writeVarInt(out, effectId);
    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {

    }

    @Override
    public int getPacketId(int protocolVersion) {
        return 0x47;
    }
}
