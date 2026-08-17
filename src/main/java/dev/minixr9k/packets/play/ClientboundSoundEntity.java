package dev.minixr9k.packets.play;

import dev.minixr9k.registries.SoundRegistry;
import dev.minixr9k.utils.MinecraftPacket;
import io.netty.buffer.ByteBuf;

import static dev.minixr9k.utils.ProtocolUtils.writeString;
import static dev.minixr9k.utils.ProtocolUtils.writeVarInt;

public class ClientboundSoundEntity implements MinecraftPacket {

    private final String sound;
    private final int category;
    private final int entityId;
    private final float volume;
    private final float pitch;
    private final long seed;

    public ClientboundSoundEntity(String sound, int category, int entityId, float volume, float pitch, long seed) {
        this.sound = sound;
        this.category = category;
        this.entityId = entityId;
        this.volume = volume;
        this.pitch = pitch;
        this.seed = seed;
    }

    @Override
    public void write(ByteBuf out, int protocolVersion) {
        int soundId = SoundRegistry.getSound(sound, protocolVersion) + 1;
        writeVarInt(out, soundId);
        if (soundId == 0) {
            writeString(out, sound);
            out.writeBoolean(false);
        }
        writeVarInt(out, category);
        writeVarInt(out, entityId);
        out.writeFloat(volume);
        out.writeFloat(pitch);
        out.writeLong(seed);
    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {

    }

    @Override
    public int getPacketId(int protocolVersion) {
        return 0x6D;
    }
}
