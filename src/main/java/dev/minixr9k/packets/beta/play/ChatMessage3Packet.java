package dev.minixr9k.packets.beta.play;

import dev.minixr9k.utils.BetaPacket;
import io.netty.buffer.ByteBuf;

import static dev.minixr9k.utils.ProtocolUtils.readBetaString;
import static dev.minixr9k.utils.ProtocolUtils.writeBetaString;

public class ChatMessage3Packet implements BetaPacket {

    private String message;

    public ChatMessage3Packet() {}

    public ChatMessage3Packet(String message) {
        this.message = message;
    }

    @Override
    public void write(ByteBuf out, int protocolVersion) {
        // Ограничение беты для исходящих сообщений — 119 символов
        if (message.length() > 119) {
            message = message.substring(0, 119);
        }
        writeBetaString(out, message);
    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {
        this.message = readBetaString(in);
    }

    public String getMessage() {
        return message;
    }

    @Override
    public int getPacketId(int protocolVersion) {
        return 0x03;
    }
}