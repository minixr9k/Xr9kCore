package dev.minixr9k.packets.beta.play;

import dev.minixr9k.utils.BetaPacket;
import dev.minixr9k.utils.ProtocolUtils;
import io.netty.buffer.ByteBuf;

public class Disconnect255Packet implements BetaPacket {

    private String reason;

    public Disconnect255Packet() {}

    public Disconnect255Packet(String reason) {
        this.reason = reason;
    }

    @Override
    public void write(ByteBuf out, int protocolVersion) {
        ProtocolUtils.writeBetaString(out, this.reason);
    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {
        this.reason = ProtocolUtils.readBetaString(in);
    }

    public String getReason() {
        return reason;
    }

    @Override
    public int getPacketId(int protocolVersion) {
        return 0xFF;
    }
}
