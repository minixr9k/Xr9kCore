package dev.minixr9k.utils;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.List;

import static dev.minixr9k.utils.ProtocolUtils.readVarInt;

public class VarIntFrameDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        in.markReaderIndex();

        for (int i = 0; i < 3; i++) {
            if (!in.isReadable()) {
                in.resetReaderIndex();
                return;
            }

            byte b = in.readByte();
            if (b >= 0) {
                in.resetReaderIndex();
                int length = readVarInt(in);

                if (in.readableBytes() < length) {
                    in.resetReaderIndex();
                    return;
                }

                out.add(in.readRetainedSlice(length));
                return;
            }
        }
    }
}