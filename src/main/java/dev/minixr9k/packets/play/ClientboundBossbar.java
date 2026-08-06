package dev.minixr9k.packets.play;

import dev.minixr9k.types.bossbar.BossBar;
import dev.minixr9k.utils.MinecraftPacket;
import io.netty.buffer.ByteBuf;

import static dev.minixr9k.utils.ProtocolUtils.*;

public class ClientboundBossbar implements MinecraftPacket {

    private final BossBar bossBar;
    private final BossBar.BossBarAction action;

    public ClientboundBossbar(BossBar bossBar, BossBar.BossBarAction action) {
        this.bossBar = bossBar;
        this.action = action;
    }

    @Override
    public void write(ByteBuf out, int protocolVersion) {
        writeUUID(out, bossBar.getUuid());
        writeVarInt(out, action.getId());
        switch (action) {
            case ADD -> {
                writeTextComponent(out, bossBar.getTitle());
                out.writeFloat(bossBar.getProgress());
                writeVarInt(out, bossBar.getColor().getId());
                writeVarInt(out, bossBar.getStyle().getId());
                out.writeByte(bossBar.getFlags());
            }
            case REMOVE -> {

            }
            case UPDATE_HEALTH -> {
                out.writeFloat(bossBar.getProgress());
            }
            case UPDATE_TITLE -> {
                writeTextComponent(out, bossBar.getTitle());
            }
            case UPDATE_STYLE -> {
                writeVarInt(out, bossBar.getColor().getId());
                writeVarInt(out, bossBar.getStyle().getId());
            }
            case UPDATE_FLAGS -> {
                out.writeByte(bossBar.getFlags());
            }
        }
    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {

    }

    @Override
    public int getPacketId(int protocolVersion) {
        return 0x09;
    }
}
