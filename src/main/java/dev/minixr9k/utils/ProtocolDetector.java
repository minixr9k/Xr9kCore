package dev.minixr9k.utils;

import dev.minixr9k.config.Configuration;
import dev.minixr9k.handlers.HandshakeState;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class ProtocolDetector extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (in.readableBytes() < 1) return;

        int firstByte = in.getUnsignedByte(in.readerIndex());

        if (firstByte == 0x02 || firstByte == 0xFE && Configuration.get().features.betaSupport) {
            ctx.pipeline().addLast("handler", new HandshakeState(ClientType.LEGACY));
        } else {
            ctx.pipeline().addLast("splitter", new VarIntFrameDecoder());
            ctx.pipeline().addLast("handler", new HandshakeState(ClientType.MODERN));
        }

        ctx.pipeline().remove(this);
    }
}