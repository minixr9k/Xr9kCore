package dev.minixr9k.api.event;

import dev.minixr9k.api.Event;
import io.netty.channel.ChannelHandlerContext;

public class HandshakeEvent extends Event {

    private final ChannelHandlerContext ctx;
    private final int protocolVersion;
    private final String host;
    private final int port;
    private final int nextState;

    public HandshakeEvent(ChannelHandlerContext ctx, int protocolVersion, String host, int port, int nextState) {
        this.ctx = ctx;
        this.protocolVersion = protocolVersion;
        this.host = host;
        this.port = port;
        this.nextState = nextState;
    }

    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    public int getProtocolVersion() {
        return protocolVersion;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public int getNextState() {
        return nextState;
    }
}
