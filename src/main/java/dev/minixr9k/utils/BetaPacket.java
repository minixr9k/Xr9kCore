package dev.minixr9k.utils;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import static dev.minixr9k.utils.ProtocolUtils.writeVarInt;

public interface BetaPacket {
    void write(ByteBuf out, int protocolVersion);

    void read(ByteBuf in, int protocolVersion);

    int getPacketId(int protocolVersion);

    default void send(ChannelHandlerContext ctx, int protocolVersion) {
        ByteBuf packet = ctx.alloc().buffer();
        try {
            packet.writeByte(getPacketId(protocolVersion));
            this.write(packet, protocolVersion);

            // 3. Отправляем напрямую в сокет
            ctx.writeAndFlush(packet);

        } catch (Exception e) {
            packet.release();
            throw e;
        }
    }
}
