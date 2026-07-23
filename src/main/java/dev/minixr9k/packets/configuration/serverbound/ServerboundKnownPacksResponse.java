package dev.minixr9k.packets.configuration.serverbound;

import dev.minixr9k.utils.MinecraftPacket;
import dev.minixr9k.utils.ProtocolUtils;
import io.netty.buffer.ByteBuf;

public class ServerboundKnownPacksResponse implements MinecraftPacket {
    @Override
    public void write(ByteBuf out, int protocolVersion) {

    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {
        int arrayLen = ProtocolUtils.readVarInt(in);

        for (int i = 0; i < arrayLen; i++) {
            String namespace = ProtocolUtils.readString(in);
            String id = ProtocolUtils.readString(in);
            String version = ProtocolUtils.readString(in);

//            System.out.printf("Pack: %s %s %s", namespace, id, version);
        }
    }

    @Override
    public int getPacketId(int protocolVersion) {
        return 0x07;
    }
}
