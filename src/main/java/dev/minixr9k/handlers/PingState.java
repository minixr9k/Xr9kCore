package dev.minixr9k.handlers;

import dev.minixr9k.packets.ping.ClientboundPong;
import dev.minixr9k.packets.ping.ClientboundStatusResponse;
import dev.minixr9k.utils.ProtocolUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class PingState extends SimpleChannelInboundHandler<ByteBuf> {

    private final int protocolVersion;

    public PingState(int protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf in) {
        int packetId = ProtocolUtils.readVarInt(in);

        if (packetId == 0x00) { // status_request
            new ClientboundStatusResponse().send(ctx, protocolVersion);
            new ClientboundPong().send(ctx, protocolVersion);
            ctx.close();
        }
        else if (packetId == 0x01) {

        }
        else {
            ctx.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
    }
}