package dev.minixr9k.packets.play;

import dev.minixr9k.utils.MinecraftPacket;
import io.netty.buffer.ByteBuf;

import static dev.minixr9k.utils.ProtocolUtils.*;

public class ClientboundUpdateAdvancements implements MinecraftPacket {

    private final String advancementId;

    public ClientboundUpdateAdvancements(String advancementId) {
        this.advancementId = advancementId;
    }

    // Дефолтный конструктор с ID по умолчанию
    public ClientboundUpdateAdvancements() {
        this("my_server:root");
    }

    @Override
    public void write(ByteBuf out, int protocolVersion) {
        // 1. Reset/Clear (Boolean) - true, чтобы полностью очистить прошлые ачивки
        out.writeBoolean(true);

        // 2. Advancement mapping (KeyPrefixed Array) -> отправляем 1 ачивку
        writeVarInt(out, 1);

        // --- Запись 1-й ачивки ---
        // 2.1 Identifier (Key)
        writeString(out, advancementId);

        // 2.2 Value (Advancement Structure)
        // Parent id (Prefixed Optional Identifier) -> null (false/0x00), так как это КОРЕНЬ
        out.writeBoolean(false);

        // Display data (Prefixed Optional Advancement display) -> true (0x01)
        out.writeBoolean(true);

        // === Advancement Display ===
        // Title (Text Component) -> NBT/JSON строка (в 1.20.5+ используются компоненты NBT, но простейший JSON стринг сработает)
        writeTextComponent(out, "Меню");

        // Description (Text Component)
        writeTextComponent(out, "Нажми L");

        // Icon (Slot)
        // Item count (VarInt)
        writeVarInt(out, 1);
        // Item type
        writeVarInt(out, 1);
        // Components count (VarInt) -> 0 (без доп. компонентов)
        out.writeBoolean(false);
        out.writeBoolean(false);

        // Flags (Int) -> 0x01 (has background texture)
        // НЕ ставим 0x02 (show_toast), чтобы не было выплывающего тоста!
        out.writeInt(0x01);

        // Background texture (Optional Identifier) -> пишем, так как во флагах стоит 0x01
        writeTextComponent(out, "minecraft:textures/block/stone.png");

        // X coord & Y coord (Float)
        out.writeFloat(0.0f);
        out.writeFloat(0.0f);
        // === Конец Advancement Display ===

        // Nested requirements (Prefixed Array) -> 0 (нет требований)
        writeVarInt(out, 0);

        // Sends telemetry data (Boolean)
        out.writeBoolean(false);
        // --- Конец записи 1-й ачивки ---

        // 3. Identifiers to remove (Prefixed Array of Identifier) -> 0
        writeVarInt(out, 0);

        // 4. Progress mapping (KeyPrefixed Array) -> 0 (не шлем прогресс)
        writeVarInt(out, 0);

        // 5. Show advancements (Boolean) -> true (показывать тост в чате если что, обычно true)
        out.writeBoolean(true);
    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {
        // Серверный пакет, считывание клиентом не требуется
    }

    @Override
    public int getPacketId(int protocolVersion) {
        if (protocolVersion >= 773) // 1.21.8+
            return 0x82;
        else if (protocolVersion >= 770) // 1.21.2 - 1.21.3
            return 0x7B;
        else
            return 0x7C; // 1.21 / 1.21.1
    }
}