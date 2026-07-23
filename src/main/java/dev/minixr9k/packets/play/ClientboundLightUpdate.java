package dev.minixr9k.packets.play;

import dev.minixr9k.utils.MinecraftPacket;
import io.netty.buffer.ByteBuf;

import java.util.*;

import static dev.minixr9k.utils.ProtocolUtils.*;

public class ClientboundLightUpdate implements MinecraftPacket {

    private final int chunkX;
    private final int chunkZ;
    private final int blockY;

    public ClientboundLightUpdate(int chunkX, int chunkZ, int blockY) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.blockY = blockY;
    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {}

    @Override
    public void write(ByteBuf out, int protocolVersion) {
        writeVarInt(out, chunkX);
        writeVarInt(out, chunkZ);

        int sectionY = (blockY - (-64)) / 16;

        BitSet skyYMask = new BitSet();
        BitSet blockYMask = new BitSet();
        BitSet emptySkyYMask = new BitSet();
        BitSet emptyBlockYMask = new BitSet();

        skyYMask.set(sectionY);
        blockYMask.set(sectionY);

        writeBitSet(out, skyYMask);
        writeBitSet(out, blockYMask);
        writeBitSet(out, emptySkyYMask);
        writeBitSet(out, emptyBlockYMask);

        // Sky Updates - отправляем данные света для обновленных секций
        List<byte[]> skyUpdates = new ArrayList<>();
        List<byte[]> blockUpdates = new ArrayList<>();

        // Создаем данные света для секции (полный свет)
        byte[] fullLightData = createFullLightData();
        skyUpdates.add(fullLightData);
        blockUpdates.add(fullLightData);

        writeVarInt(out, skyUpdates.size());
        for (byte[] update : skyUpdates) {
            writeByteArray(out, update);
        }

        // Записываем block updates
        writeVarInt(out, blockUpdates.size());
        for (byte[] update : blockUpdates) {
            writeByteArray(out, update);
        }

    }

    public static byte[] createFullLightData() {
        // Создаем данные для полного света (16x16x16 секция)
        // Каждый байт содержит свет для 2 блоков (4 бита на блок)
        byte[] lightData = new byte[2048]; // 16*16*16/2 = 2048 байт
        Arrays.fill(lightData, (byte) 0xFF); // Максимальный свет (15) для всех блоков
        return lightData;
    }

    @Override
    public int getPacketId(int protocolVersion) {
        if (protocolVersion == 769 || protocolVersion == 768)
            return 0x2B;
        else if (protocolVersion < 773)
            return 0x2A;
        else
            return 0x2F;
    }
}