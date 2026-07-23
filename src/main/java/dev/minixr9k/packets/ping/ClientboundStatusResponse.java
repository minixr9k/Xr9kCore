package dev.minixr9k.packets.ping;

import dev.minixr9k.features.World;
import dev.minixr9k.utils.MinecraftPacket;
import io.netty.buffer.ByteBuf;

import static dev.minixr9k.utils.ProtocolUtils.writeString;

public class ClientboundStatusResponse implements MinecraftPacket {

    @Override
    public void read(ByteBuf in, int protocolVersion) {}

    @Override
    public void write(ByteBuf out, int protocolVersion) {
        String status = "{\"version\":{\"name\":\"MiniCore\",\"protocol\":" + protocolVersion + "},\"players\":{\"max\":" + -1 + ",\"online\":" + World.getAllPlayers().size() + "},\"description\":{\"text\":\"" + "A MiniCore server (1.21.8)" + "\"}}";
        writeString(out, status);
    }

    @Override
    public int getPacketId(int protocolVersion) {
        return 0x00;
    }
}
