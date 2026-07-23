package dev.minixr9k.utils;

import io.netty.buffer.ByteBuf;

import static dev.minixr9k.utils.ProtocolUtils.writeVarInt;

public interface MinecraftPacket {
    void write(ByteBuf out, int protocolVersion);

    void read(ByteBuf in, int protocolVersion);

    int getPacketId(int protocolVersion);

    default void send(io.netty.channel.ChannelHandlerContext ctx, int protocolVersion) {
        ByteBuf data = ctx.alloc().buffer();
        try {
            writeVarInt(data, getPacketId(protocolVersion));
            this.write(data, protocolVersion);

            int packetLength = data.readableBytes();

            // Выделяем финальный буфер [Длина] + [Данные]
            ByteBuf packet = ctx.alloc().buffer();

            writeVarInt(packet, packetLength);
            packet.writeBytes(data);

            ctx.writeAndFlush(packet);

        } finally {
            data.release();
        }
    }
}
