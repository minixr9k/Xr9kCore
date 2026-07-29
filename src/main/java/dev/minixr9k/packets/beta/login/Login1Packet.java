package dev.minixr9k.packets.beta.login;

import dev.minixr9k.utils.BetaPacket;
import io.netty.buffer.ByteBuf;

import static dev.minixr9k.utils.ProtocolUtils.readBetaString;
import static dev.minixr9k.utils.ProtocolUtils.writeBetaString;

public class Login1Packet implements BetaPacket {

    private int protocolOrEntityId;
    private String username;
    private long seed;
    private byte dimension;

    public Login1Packet() {}

    public Login1Packet(int protocolOrEntityId, long seed, byte dimension) {
        this.protocolOrEntityId = protocolOrEntityId;
        this.seed = seed;
        this.dimension = dimension;
    }

    @Override
    public void write(ByteBuf out, int protocolVersion) {
        out.writeInt(protocolOrEntityId);
        writeBetaString(out, ""); // unused
        out.writeLong(seed);
        out.writeByte(dimension);
    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {
        this.protocolOrEntityId = in.readInt();
        this.username = readBetaString(in);
        in.readLong(); // unused
        in.readByte(); // unused
    }

    public int getProtocolVersion() {
        return protocolOrEntityId;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public int getPacketId(int protocolVersion) {
        return 0x01;
    }
}
