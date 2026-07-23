package dev.minixr9k.packets.login;

import dev.minixr9k.utils.MinecraftPacket;
import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

import static dev.minixr9k.utils.ProtocolUtils.writeVarInt;

public class ClientboundLoginDisconnect implements MinecraftPacket {

    private final String reason;

    public ClientboundLoginDisconnect(String reason) {
        this.reason = "{\"text\":\"" + reason + "\"}";
    }

    @Override
    public void write(ByteBuf out, int protocolVersion) {
        byte[] bytes = reason.getBytes(StandardCharsets.UTF_8);
        writeVarInt(out, bytes.length);
        out.writeBytes(bytes);
    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {

    }

    @Override
    public int getPacketId(int protocolVersion) {
        return 0x00;
    }
}
