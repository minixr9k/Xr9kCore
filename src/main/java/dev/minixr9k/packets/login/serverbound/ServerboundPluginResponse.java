package dev.minixr9k.packets.login.serverbound;

import dev.minixr9k.utils.MinecraftPacket;
import io.netty.buffer.ByteBuf;

import static dev.minixr9k.utils.ProtocolUtils.readStringWithShort;
import static dev.minixr9k.utils.ProtocolUtils.readVarInt;

public class ServerboundPluginResponse implements MinecraftPacket {

    private int messageId;
    private boolean successful;
    private ByteBuf data;

    @Override
    public void write(ByteBuf out, int protocolVersion) {

    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {
        this.messageId = readVarInt(in);
        this.successful = in.readBoolean();
        if (this.successful && in.isReadable()) {
            // Сохраняем оставшийся slice буфера
            this.data = in.readRetainedSlice(in.readableBytes());
        }
    }

    public int getMessageId() {
        return messageId;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public ByteBuf getData() {
        return data;
    }

    @Override
    public int getPacketId(int protocolVersion) {
        return 0x02;
    }
}
