package dev.minixr9k.packets.confAndPlay;

import dev.minixr9k.utils.MinecraftPacket;
import io.netty.buffer.ByteBuf;

import java.util.UUID;

import static dev.minixr9k.utils.ProtocolUtils.*;

public class ClientboundResourcepackPush implements MinecraftPacket {

    private final UUID uuid;
    private final String url;
    private final String sha1;
    private final boolean forced;
    private final String prompt;

    public ClientboundResourcepackPush(UUID uuid, String url, String sha1, boolean forced, String prompt) {
        this.uuid = uuid;
        this.url = url;
        this.sha1 = sha1;
        this.forced = forced;
        this.prompt = prompt;
    }

    @Override
    public void write(ByteBuf out, int protocolVersion) {
        writeUUID(out, uuid);
        writeString(out, url);
        writeString(out, sha1);
        out.writeBoolean(forced);
        if (prompt.isEmpty())
            out.writeBoolean(false);
        else {
            out.writeBoolean(true);
            writeTextComponent(out, prompt);
        }
    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {

    }

    @Override
    public int getPacketId(int protocolVersion) {
        if (protocolVersion > 772)
            return 0x4F;
        return 0x4A;
    }
}
