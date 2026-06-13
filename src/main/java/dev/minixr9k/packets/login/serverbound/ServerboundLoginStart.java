package dev.minixr9k.packets.login.serverbound;

import dev.minixr9k.utils.MinecraftPacket;
import dev.minixr9k.utils.ProtocolUtils;
import io.netty.buffer.ByteBuf;

import java.util.UUID;

public class ServerboundLoginStart implements MinecraftPacket {

    private String username;
    private UUID uuid;

    @Override
    public void write(ByteBuf out, int protocolVersion) {

    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {
        this.username = ProtocolUtils.readString(in);
        this.uuid = ProtocolUtils.readUUID(in);
    }

    @Override
    public int getPacketId(int protocolVersion) {
        return 0x00;
    }

    public String getUsername() { return username; }
    public UUID getUuid() { return uuid; }
}
