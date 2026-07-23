package dev.minixr9k.packets.play;

import dev.minixr9k.auth.PlayerProfile;
import dev.minixr9k.utils.MinecraftPacket;
import io.netty.buffer.ByteBuf;

import java.util.List;
import java.util.UUID;

import static dev.minixr9k.utils.ProtocolUtils.*;

public class ClientboundPlayerInfoUpdate implements MinecraftPacket {

    private final int actionsMask;
    private final UUID uuid;
    private final String playerName;
    private final List<PlayerProfile> properties;

    public ClientboundPlayerInfoUpdate(int actionsMask, UUID uuid, String playerName, List<PlayerProfile> properties) {
        this.actionsMask = actionsMask;
        this.uuid = uuid;
        this.playerName = playerName;
        this.properties = properties;
    }

    @Override
    public void write(ByteBuf out, int protocolVersion) {
        // Actions (битовая маска действий)
        writeVarInt(out, actionsMask);

        // Количество игроков (всегда 1 в этом простом примере)
        writeVarInt(out, 1);

        // UUID игрока
        writeUUID(out, uuid);

        if ((actionsMask & 0x01) != 0) {
            writeString(out, playerName);

            if (properties == null) {
                writeVarInt(out, 0);
            }
            else {
                writeVarInt(out, properties.size());

                for (PlayerProfile property : properties) {
                    writeString(out, property.getName());
                    writeString(out, property.getValue());

                    if (property.getSignature() != null) {
                        out.writeBoolean(true);
                        writeString(out, property.getSignature());
                    } else {
                        out.writeBoolean(false);
                    }
                }
            }
        }

        if ((actionsMask & 0x04) != 0) {
            writeVarInt(out, 1);
        }

        // 4. Update Listed (0x08)
        if ((actionsMask & 0x08) != 0) {
            out.writeBoolean(true);
        }

        // 5. Update Latency (0x10)
        if ((actionsMask & 0x10) != 0) {
            writeVarInt(out, 1);
        }

        // 6. Update Display Name (0x20) - пропускаем в простом варианте
        if ((actionsMask & 0x20) != 0) {
            // null - нет отображаемого имени
            out.writeByte(0); // Префикс 0 для Optional
        }

        // 7. Update List Priority (0x40) - пропускаем в простом варианте
        if ((actionsMask & 0x40) != 0) {
            writeVarInt(out, 0); // Стандартный приоритет
        }

        // 8. Update Hat (0x80) - пропускаем в простом варианте
        if ((actionsMask & 0x80) != 0) {
            out.writeBoolean(true); // Шляпа видна
        }


    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {

    }

    @Override
    public int getPacketId(int protocolVersion) {
        if (protocolVersion == 767)
            return 0x3E;
        else if (protocolVersion >= 773)
            return 0x44;
        else return 0x3F;
    }
}
