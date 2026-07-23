package dev.minixr9k.packets.handshake;

import dev.minixr9k.utils.MinecraftPacket;
import io.netty.buffer.ByteBuf;

import static dev.minixr9k.utils.ProtocolUtils.readString;
import static dev.minixr9k.utils.ProtocolUtils.readVarInt;

public class Handshake implements MinecraftPacket {

    private int protocolVersion;
    private String serverAddress;
    private int serverPort;
    private int nextState;

    @Override
    public void write(ByteBuf out, int protocolVersion) {

    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {
        this.protocolVersion = readVarInt(in);
        this.serverAddress = readString(in);
        this.serverPort = in.readUnsignedShort();
        this.nextState = readVarInt(in);
    }

    @Override
    public int getPacketId(int protocolVersion) {
        return 0x00;
    }

    public int getProtocolVersion() {
        return protocolVersion;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public int getServerPort() {
        return serverPort;
    }

    public int getNextState() {
        return nextState;
    }
}
