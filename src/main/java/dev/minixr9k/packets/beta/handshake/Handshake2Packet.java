package dev.minixr9k.packets.beta.handshake;

import dev.minixr9k.utils.BetaPacket;
import io.netty.buffer.ByteBuf;

import static dev.minixr9k.utils.ProtocolUtils.readBetaString;
import static dev.minixr9k.utils.ProtocolUtils.writeBetaString;

public class Handshake2Packet implements BetaPacket {

    private String username;

    public Handshake2Packet() {}

    public Handshake2Packet(String username) {
        this.username = username;
    }

    @Override
    public void write(ByteBuf out, int protocolVersion) {
        writeBetaString(out, username);
    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {
        this.username = readBetaString(in);
    }

    public String getUsername() {
        return username;
    }

    @Override
    public int getPacketId(int protocolVersion) {
        return 0x02;
    }
}
