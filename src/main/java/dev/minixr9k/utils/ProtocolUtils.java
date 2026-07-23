package dev.minixr9k.utils;

import com.google.gson.*;
import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;
import java.util.BitSet;
import java.util.UUID;

public class ProtocolUtils {

    public static int readVarInt(ByteBuf input) {
        int value = 0;
        int i = 0;
        byte b;
        while (((b = input.readByte()) & 0x80) != 0) {
            value |= (b & 0x7F) << i;
            i += 7;
            if (i > 35) throw new RuntimeException("VarInt is too big");
        }
        return value | (b << i);
    }

    public static String readString(ByteBuf in) {
        int len = readVarInt(in);
        byte[] bytes = new byte[len];
        in.readBytes(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static UUID readUUID(ByteBuf in) {
        return new UUID(in.readLong(), in.readLong());
    }

    public static void writeVarInt(ByteBuf out, int value) {
        while ((value & 0xFFFFFF80) != 0) {
            out.writeByte((value & 0x7F) | 0x80);
            value >>>= 7;
        }
        out.writeByte(value);
    }

    public static void writeString(ByteBuf out, String str) {
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        writeVarInt(out, bytes.length);
        out.writeBytes(bytes);
    }

    public static void writeUUID(ByteBuf out, UUID uuid) {
        out.writeLong(uuid.getMostSignificantBits());
        out.writeLong(uuid.getLeastSignificantBits());
    }

    public static void writeTextComponent(ByteBuf buf, String message){
        if (message.contains("\"text\":")) {
            writeColoredTextComponent(buf, message);
            return;
        }
        buf.writeByte(8); // Тип тега String
        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
        buf.writeShort(messageBytes.length);
        buf.writeBytes(messageBytes);
    }

    public static void writeColoredTextComponent(ByteBuf buf, String message) {
        JsonArray jsonArray = JsonParser.parseString(message).getAsJsonArray();

        // NBT List (ID 9)
        buf.writeByte(9);

        // TAG_Compound (ID 10)
        buf.writeByte(10);

        // list count (int)
        buf.writeInt(jsonArray.size());

        for (JsonElement el : jsonArray) {
            JsonObject obj = el.getAsJsonObject();
            String text = obj.get("text").getAsString();

            // NBT String (ID 8)
            buf.writeByte(8);
            writeNbtField(buf, "text", text);

            if (obj.has("color")) {
                String color = obj.get("color").getAsString();

                // NBT String (ID 8)
                buf.writeByte(8);
                writeNbtField(buf, "color", color);
            }

            // TAG_End (ID 0)
            buf.writeByte(0);
        }
    }

    // Вспомогательный метод для записи структуры "Имя поля" + "Значение"
    private static void writeNbtField(ByteBuf buf, String key, String value) {
        writeStringValue(buf, key);
        writeStringValue(buf, value);
    }

    // Утилита для записи строки в формате Minecraft NBT (Размер short + байты)
    private static void writeStringValue(ByteBuf buf, String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        buf.writeShort(bytes.length);
        buf.writeBytes(bytes);
    }

    public static void writeBitSet(ByteBuf buf, BitSet bitSet) {
        if (bitSet == null || bitSet.isEmpty()) {
            writeVarInt(buf, 0);
            return;
        }

        long[] data = bitSet.toLongArray();

        writeVarInt(buf, data.length);

        for (long value : data) {
            buf.writeLong(value);
        }
    }

    public static void writeByteArray(ByteBuf buf, byte[] data) {
        writeVarInt(buf, data.length);
        buf.writeBytes(data);
    }

    public static void writeStringWithShort(ByteBuf buf, String s) {
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        buf.writeShort(bytes.length);
        buf.writeBytes(bytes);
    }

    public static String readStringWithShort(ByteBuf buf) {
        int len = buf.readShort();
        byte[] bytes = new byte[len];
        buf.readBytes(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }
}