package dev.minixr9k.packets.login;

import dev.minixr9k.utils.MinecraftPacket;
import dev.minixr9k.utils.ProtocolUtils;
import io.netty.buffer.ByteBuf;
import java.util.UUID;

public class ClientboundLoginSuccessPacket implements MinecraftPacket {
    private final UUID uuid;
    private final String username;

    public ClientboundLoginSuccessPacket(UUID uuid, String username) {
        this.uuid = uuid;
        this.username = username;
    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {}

    @Override
    public void write(ByteBuf out, int protocolVersion) {
        ProtocolUtils.writeUUID(out, uuid);
        ProtocolUtils.writeString(out, username);
        ProtocolUtils.writeVarInt(out, 0); // Properties size = 0 (No skins)

        if (protocolVersion <= 767)
            out.writeBoolean(false);
    }

    @Override
    public int getPacketId(int protocolVersion) { return 0x02; }
}