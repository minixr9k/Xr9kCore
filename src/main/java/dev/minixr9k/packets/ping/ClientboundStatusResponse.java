package dev.minixr9k.packets.ping;

import dev.minixr9k.config.Configuration;
import dev.minixr9k.features.World;
import dev.minixr9k.utils.MinecraftPacket;
import io.netty.buffer.ByteBuf;

import static dev.minixr9k.utils.ProtocolUtils.writeString;

public class ClientboundStatusResponse implements MinecraftPacket {

    @Override
    public void read(ByteBuf in, int protocolVersion) {}

    @Override
    public void write(ByteBuf out, int protocolVersion) {
        String status = "";
        if (Configuration.get().features.pingSameProtocol)
            status = "{\"version\":{\"name\":\"MiniCore\",\"protocol\":" + protocolVersion + "},\"players\":{\"max\":" + Configuration.get().motd.maxPlayers + ",\"online\":" + World.getAllPlayers().size() + "},\"description\":{\"text\":\"" + Configuration.get().motd.text + "\"}}";
        else
            status = "{\"version\":{\"name\":\"MiniCore\",\"protocol\":" + Configuration.get().features.mainProtocol + "},\"players\":{\"max\":" + Configuration.get().motd.maxPlayers + ",\"online\":" + World.getAllPlayers().size() + "},\"description\":{\"text\":\"" + Configuration.get().motd.text + "\"}}";
        writeString(out, status);
    }

    @Override
    public int getPacketId(int protocolVersion) {
        return 0x00;
    }
}
