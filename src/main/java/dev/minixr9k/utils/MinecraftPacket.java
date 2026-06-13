package dev.minixr9k.utils;

import io.netty.buffer.ByteBuf;

import static dev.minixr9k.utils.ProtocolUtils.writeVarInt;

public interface MinecraftPacket {
    void write(ByteBuf out, int protocolVersion);

    void read(ByteBuf in, int protocolVersion);

    int getPacketId(int protocolVersion);

    default void send(io.netty.channel.ChannelHandlerContext ctx, int protocolVersion) {
        ByteBuf data = ctx.alloc().buffer();
        writeVarInt(data, getPacketId(protocolVersion));
        this.write(data, protocolVersion);

        ByteBuf header = ctx.alloc().buffer();
        writeVarInt(header, data.readableBytes());

        ctx.write(header);
        ctx.writeAndFlush(data);
    }
}
