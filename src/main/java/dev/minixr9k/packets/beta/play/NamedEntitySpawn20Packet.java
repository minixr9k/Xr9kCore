package dev.minixr9k.packets.beta.play;

import dev.minixr9k.utils.BetaPacket;
import io.netty.buffer.ByteBuf;

import static dev.minixr9k.utils.ProtocolUtils.readBetaString;
import static dev.minixr9k.utils.ProtocolUtils.writeBetaString;

public class NamedEntitySpawn20Packet implements BetaPacket {

    private int entityId;
    private String name;
    private int x;
    private int y;
    private int z;
    private byte rotation;
    private byte pitch;
    private short currentItem;

    public NamedEntitySpawn20Packet() {}

    // Удобный конструктор, принимающий обычные double-координаты и yaw/pitch
    public NamedEntitySpawn20Packet(int entityId, String name, double x, double y, double z, float yaw, float pitch, short currentItem) {
        this.entityId = entityId;
        this.name = name;
        this.x = (int) Math.floor(x * 32.0D);
        this.y = (int) Math.floor(y * 32.0D);
        this.z = (int) Math.floor(z * 32.0D);
        this.rotation = (byte) (yaw * 256.0F / 360.0F);
        this.pitch = (byte) (pitch * 256.0F / 360.0F);
        this.currentItem = currentItem < 0 ? 0 : currentItem; // Не даем уйти в негатив
    }

    @Override
    public void write(ByteBuf out, int protocolVersion) {
        out.writeInt(entityId);
        writeBetaString(out, name);
        out.writeInt(x);
        out.writeInt(y);
        out.writeInt(z);
        out.writeByte(rotation);
        out.writeByte(pitch);
        out.writeShort(currentItem);
    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {
        this.entityId = in.readInt();
        this.name = readBetaString(in);
        this.x = in.readInt();
        this.y = in.readInt();
        this.z = in.readInt();
        this.rotation = in.readByte();
        this.pitch = in.readByte();
        this.currentItem = in.readShort();
    }

    public int getEntityId() { return entityId; }
    public String getName() { return name; }
    public double getRealX() { return x / 32.0D; }
    public double getRealY() { return y / 32.0D; }
    public double getRealZ() { return z / 32.0D; }
    public short getCurrentItem() { return currentItem; }

    @Override
    public int getPacketId(int protocolVersion) {
        return 0x14;
    }
}