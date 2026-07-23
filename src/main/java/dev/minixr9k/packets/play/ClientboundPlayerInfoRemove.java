package dev.minixr9k.packets.play;

import dev.minixr9k.utils.MinecraftPacket;
import io.netty.buffer.ByteBuf;

import java.util.UUID;

import static dev.minixr9k.utils.ProtocolUtils.writeUUID;
import static dev.minixr9k.utils.ProtocolUtils.writeVarInt;

public class ClientboundPlayerInfoRemove implements MinecraftPacket {

    private UUID uuid;

    public ClientboundPlayerInfoRemove(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public void write(ByteBuf out, int protocolVersion) {
        writeVarInt(out, 1);
        writeUUID(out, uuid);
    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {

    }

    @Override
    public int getPacketId(int protocolVersion) {
        if (protocolVersion == 767)
            return 0x3D;
        else if (protocolVersion >= 773)
            return 0x43;
        else return 0x3E;
    }
}
