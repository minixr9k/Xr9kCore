package dev.minixr9k.registries;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import java.nio.charset.StandardCharsets;

public class RegistryDataSender {

    public void sendWolfVariantAddAngry(ChannelHandlerContext ctx) {
        ByteBuf pBuf = ctx.alloc().buffer();

        try {
            writeVarInt(pBuf, 0x07);
            writeVarInt(pBuf, 22);
            pBuf.writeBytes("minecraft:wolf_variant".getBytes(StandardCharsets.UTF_8));
            writeVarInt(pBuf, 1);
            writeVarInt(pBuf, 15);
            pBuf.writeBytes("minecraft:ashen".getBytes(StandardCharsets.UTF_8));

            ByteBuf nbtBuf = ctx.alloc().buffer();
            try {
                nbtBuf.writeByte(0x0A);

                // id
                nbtBuf.writeByte(0x03);
                writeNBTString(nbtBuf, "id");
                nbtBuf.writeInt(0);

                // element
                nbtBuf.writeByte(0x0A);
                writeNBTString(nbtBuf, "element");

                // assets
                nbtBuf.writeByte(0x0A);
                writeNBTString(nbtBuf, "assets");

                // tame
                nbtBuf.writeByte(0x08);
                writeNBTString(nbtBuf, "tame");
                writeNBTString(nbtBuf, "minecraft:entity/wolf/wolf_ashen_tame");

                // angry - добавляем второе поле
                nbtBuf.writeByte(0x08);
                writeNBTString(nbtBuf, "wild");
                writeNBTString(nbtBuf, "minecraft:entity/wolf/wolf_ashen");

                nbtBuf.writeByte(0x00); // закрыть assets
                nbtBuf.writeByte(0x00); // закрыть element
                nbtBuf.writeByte(0x00); // закрыть корневой compound

                writeVarInt(pBuf, nbtBuf.readableBytes());
                pBuf.writeBytes(nbtBuf);

            } finally {
                nbtBuf.release();
            }

            sendPacket(ctx, pBuf);
            System.out.println("Sent: id + element + assets with tame + angry");

        } finally {
            // Буфер гарантированно удалится и при успехе, и при ошибке
            if (pBuf != null && pBuf.refCnt() > 0) {
                pBuf.release();
            }
        }
    }

    public void sendOverworldDimensionRegistry(ChannelHandlerContext ctx) {
        ByteBuf pBuf = ctx.alloc().buffer();

        try {
            writeVarInt(pBuf, 0x07); // ID пакета registry_data в Configuration

            // 1. Имя реестра (Identifier: VarInt длина + строка)
            writeString(pBuf, "minecraft:dimension_type");

            // 2. Количество записей (Prefixed Array) -> отправляем 1 запись
            writeVarInt(pBuf, 1);

            // 3. Имя записи (Identifier)
            writeString(pBuf, "minecraft:overworld");

            // 4. Флаг присутствия NBT (Prefixed Optional NBT) -> true
            pBuf.writeBoolean(true);

            ByteBuf nbtBuf = ctx.alloc().buffer();
            try {
                // Корневой Compound (В сетевом NBT имя ПУСТОЕ = short 0)
                nbtBuf.writeByte(10); // TAG_Compound
//
//
//                // --- Начинка element ---
//
//                // "piglin_safe": false (В NBT булеаны пишутся как TAG_Byte = 1)
                nbtBuf.writeByte(1);
                writeNBTString(nbtBuf, "piglin_safe");
                nbtBuf.writeByte(0);
//
//                // "natural": true
                nbtBuf.writeByte(1);
                writeNBTString(nbtBuf, "natural");
                nbtBuf.writeByte(1);
//
//                // "ambient_light": 0.0 (TAG_Float = 5)
                nbtBuf.writeByte(5);
                writeNBTString(nbtBuf, "ambient_light");
                nbtBuf.writeFloat(0.0f);
//
//                // "monster_spawn_block_light_limit": 0 (TAG_Int = 3)
                nbtBuf.writeByte(3);
                writeNBTString(nbtBuf, "monster_spawn_block_light_limit");
                nbtBuf.writeInt(0);
//
//                // "infiniburn": "#minecraft:infiniburn_overworld" (TAG_String = 8)
                nbtBuf.writeByte(8);
                writeNBTString(nbtBuf, "infiniburn");
                writeNBTString(nbtBuf, "#minecraft:infiniburn_overworld");
//
//                // "respawn_anchor_works": false
                nbtBuf.writeByte(1);
                writeNBTString(nbtBuf, "respawn_anchor_works");
                nbtBuf.writeByte(0);
//
//                // "has_skylight": true
                nbtBuf.writeByte(1);
                writeNBTString(nbtBuf, "has_skylight");
                nbtBuf.writeByte(1);
//
//                // "bed_works": true
                nbtBuf.writeByte(1);
                writeNBTString(nbtBuf, "bed_works");
                nbtBuf.writeByte(1);
//
//                // "effects": "minecraft:overworld"
                nbtBuf.writeByte(8);
                writeNBTString(nbtBuf, "effects");
//                writeNBTString(nbtBuf, "minecraft:overworld");
                writeNBTString(nbtBuf, "minecraft:the_nether");
//
//                // "has_raids": true
                nbtBuf.writeByte(1);
                writeNBTString(nbtBuf, "has_raids");
                nbtBuf.writeByte(1);
//
//                // "logical_height": 384
                nbtBuf.writeByte(3);
                writeNBTString(nbtBuf, "logical_height");
                nbtBuf.writeInt(384);
//
//                // "coordinate_scale": 1.0 (TAG_Double = 6)
                nbtBuf.writeByte(6);
                writeNBTString(nbtBuf, "coordinate_scale");
                nbtBuf.writeDouble(1.0);
//
//                // "cloud_height": 192 (TAG_Int = 3)
                nbtBuf.writeByte(3);
                writeNBTString(nbtBuf, "cloud_height");
                nbtBuf.writeInt(192);
//
//                // "monster_spawn_light_level" - в 1.21.x это Compound (интервал uniform)
                nbtBuf.writeByte(10);
                writeNBTString(nbtBuf, "monster_spawn_light_level");

                nbtBuf.writeByte(3); // min_inclusive
                writeNBTString(nbtBuf, "min_inclusive");
                nbtBuf.writeInt(0);

                nbtBuf.writeByte(3); // max_inclusive
                writeNBTString(nbtBuf, "max_inclusive");
                nbtBuf.writeInt(7);

                nbtBuf.writeByte(8); // type
                writeNBTString(nbtBuf, "type");
                writeNBTString(nbtBuf, "minecraft:uniform");

                nbtBuf.writeByte(0); // Закрываем monster_spawn_light_level

//                // "attributes" - в 1.21.x это Compound (интервал uniform)
                nbtBuf.writeByte(10);
                writeNBTString(nbtBuf, "attributes");

                nbtBuf.writeByte(8); // minecraft:visual/fog_color (если не записывать то облаков не будет)
                writeNBTString(nbtBuf, "minecraft:visual/cloud_color");
                writeNBTString(nbtBuf, "#ccffffff");

                nbtBuf.writeByte(5);
                writeNBTString(nbtBuf, "minecraft:visual/cloud_height");
                nbtBuf.writeFloat(192.33f);

                nbtBuf.writeByte(8); // minecraft:visual/fog_color
                writeNBTString(nbtBuf, "minecraft:visual/fog_color");
                writeNBTString(nbtBuf, "#c0d8ff");

                nbtBuf.writeByte(8); // minecraft:visual/sky_color
                writeNBTString(nbtBuf, "minecraft:visual/sky_color");
                writeNBTString(nbtBuf, "#78a7ff");

                // необязательные настройки
                nbtBuf.writeByte(5);
                writeNBTString(nbtBuf, "minecraft:visual/fog_start_distance");
                nbtBuf.writeFloat(10f);

                nbtBuf.writeByte(5);
                writeNBTString(nbtBuf, "minecraft:visual/fog_end_distance");
                nbtBuf.writeFloat(48f);
                //

                nbtBuf.writeByte(0); // Закрываем attributes
//
//                // "min_y": -64
                nbtBuf.writeByte(3);
                writeNBTString(nbtBuf, "min_y");
                nbtBuf.writeInt(-64);
//
//                // "has_ceiling": false
                nbtBuf.writeByte(1);
                writeNBTString(nbtBuf, "has_ceiling");
                nbtBuf.writeByte(0);
//
//                // "ultrawarm": false
                nbtBuf.writeByte(1);
                writeNBTString(nbtBuf, "ultrawarm");
                nbtBuf.writeByte(0);
//
//                // "height": 384
                nbtBuf.writeByte(3);
                writeNBTString(nbtBuf, "height");
                nbtBuf.writeInt(384);
//
//                // --- Закрываем структуру ---
                nbtBuf.writeByte(0); // закрыть корневой compound (TAG_End)

                // Записываем NBT-буфер в основной
                pBuf.writeBytes(nbtBuf);

            } finally {
                nbtBuf.release();
            }

            sendPacket(ctx, pBuf);
//            System.out.println("Sent dimension_type: overworld with full NBT");

        } finally {
            // Буфер гарантированно удалится и при успехе, и при ошибке
            if (pBuf != null && pBuf.refCnt() > 0) {
                pBuf.release();
            }
        }
    }

    public void sendBiomeRegistry(ChannelHandlerContext ctx) {
        ByteBuf pBuf = ctx.alloc().buffer();

        try {
            writeVarInt(pBuf, 0x07); // ID пакета registry_data в Configuration

            // 1. Имя реестра (Identifier: VarInt длина + строка)
            writeString(pBuf, "minecraft:worldgen/biome");

            // 2. Количество записей (Prefixed Array) -> отправляем 1 запись
            writeVarInt(pBuf, 2);

            // 3. Имя записи (Identifier)
            writeString(pBuf, "minecraft:plains");
            pBuf.writeBoolean(false);
            writeString(pBuf, "minecraft:forest");

            // 4. Флаг присутствия NBT (Prefixed Optional NBT) -> true
            pBuf.writeBoolean(false);

            sendPacket(ctx, pBuf);
//            System.out.println("Sent minecraft:worldgen/biome overworld with full NBT");

        } finally {
            // Буфер гарантированно удалится и при успехе, и при ошибке
            if (pBuf != null && pBuf.refCnt() > 0) {
                pBuf.release();
            }
        }
    }

    public void sendWolfVariants(ChannelHandlerContext ctx) {
        ByteBuf pBuf = ctx.alloc().buffer();

        try {
            // ID пакета (например, Registry Data)
            writeVarInt(pBuf, 0x07);

            // Идентификатор реестра
            writeString(pBuf, "minecraft:wolf_variant");

            String[] variants = {"minecraft:ashen",
                                "minecraft:black",
                                "minecraft:chestnut",
                                "minecraft:pale",
                                "minecraft:rusty",
                                "minecraft:snowy",
                                "minecraft:spotted",
                                "minecraft:striped",
                                "minecraft:woods"};

            // Записываем количество элементов в реестре (VarInt)
            writeVarInt(pBuf, variants.length);

            for (String variant : variants) {

                // 1. Имя элемента (Identifier)
                writeString(pBuf, variant);

                // 2. Флаг "Has Data" (true = 1)
                pBuf.writeBoolean(false);
            }

            sendPacket(ctx, pBuf);
//            System.out.println("Sent " + variants.length + " wolf variants successfully.");

        } finally {
            // Буфер гарантированно удалится и при успехе, и при ошибке
            if (pBuf != null && pBuf.refCnt() > 0) {
                pBuf.release();
            }
        }
    }

    public void sendZombieNautilusVariants(ChannelHandlerContext ctx) {
        ByteBuf pBuf = ctx.alloc().buffer();

        try {
            // ID пакета (например, Registry Data)
            writeVarInt(pBuf, 0x07);

            // Идентификатор реестра
            writeString(pBuf, "minecraft:zombie_nautilus_variant");

            String[] variants = {
                    "minecraft:temperate",
                    "minecraft:warm"};

            // Записываем количество элементов в реестре (VarInt)
            writeVarInt(pBuf, variants.length);

            for (String variant : variants) {

                // 1. Имя элемента (Identifier)
                writeString(pBuf, variant);

                // 2. Флаг "Has Data" (true = 1)
                pBuf.writeBoolean(false);
            }

            sendPacket(ctx, pBuf);
//            System.out.println("Sent " + variants.length + " wolf variants successfully.");

        } finally {
            // Буфер гарантированно удалится и при успехе, и при ошибке
            if (pBuf != null && pBuf.refCnt() > 0) {
                pBuf.release();
            }
        }
    }

    public void sendTimeLine(ChannelHandlerContext ctx) {
        ByteBuf pBuf = ctx.alloc().buffer();

        try {
            // ID пакета (например, Registry Data)
            writeVarInt(pBuf, 0x07);

            // Идентификатор реестра
            writeString(pBuf, "minecraft:timeline");

            String[] variants = {
                    "minecraft:day",
                    "minecraft:early_game",
                    "minecraft:moon",
                    "minecraft:villager_schedule"};

            // Записываем количество элементов в реестре (VarInt)
            writeVarInt(pBuf, variants.length);

            for (String variant : variants) {

                // 1. Имя элемента (Identifier)
                writeString(pBuf, variant);

                // 2. Флаг "Has Data" (true = 1)
                pBuf.writeBoolean(false);
            }

            sendPacket(ctx, pBuf);
//            System.out.println("Sent " + variants.length + " wolf variants successfully.");

        } finally {
            // Буфер гарантированно удалится и при успехе, и при ошибке
            if (pBuf != null && pBuf.refCnt() > 0) {
                pBuf.release();
            }
        }
    }

    public void sendCatVariants(ChannelHandlerContext ctx) {
        ByteBuf pBuf = ctx.alloc().buffer();

        try {
            // 1. ID пакета Registry Data (Configuration State)
            writeVarInt(pBuf, 0x07);

            // 2. Идентификатор реестра
            writeString(pBuf, "minecraft:cat_variant");

            // Список всех ванильных вариантов кошек для 1.21.8
            String[] variants = {
                    "minecraft:all_black",
                    "minecraft:black",
                    "minecraft:british_shorthair",
                    "minecraft:calico",
                    "minecraft:jellie",
                    "minecraft:persian",
                    "minecraft:ragdoll",
                    "minecraft:red",
                    "minecraft:siamese",
                    "minecraft:tabby",
                    "minecraft:white"
            };

            // 3. Записываем количество элементов в реестре (Prefixed Array)
            writeVarInt(pBuf, variants.length);

            for (String variant : variants) {
                // Имя элемента (Identifier)
                writeString(pBuf, variant);

                // Флаг "Has Data" (false = 0), данные подтянутся из minecraft:core
                pBuf.writeBoolean(false);
            }

            // Передаем буфер в метод отправки, который сам сделает release()
            sendPacket(ctx, pBuf);
//            System.out.println("Sent " + variants.length + " cat variants successfully.");

        } finally {
            // Буфер гарантированно удалится и при успехе, и при ошибке
            if (pBuf != null && pBuf.refCnt() > 0) {
                pBuf.release();
            }
        }
    }

    public void sendWolfSoundVariants(ChannelHandlerContext ctx) {
        ByteBuf pBuf = ctx.alloc().buffer();
        try {
            writeVarInt(pBuf, 0x07); // Packet ID: registry_data
            writeString(pBuf, "minecraft:wolf_sound_variant");

            String[] variants = {
                    "minecraft:angry",
                    "minecraft:big",
                    "minecraft:classic",
                    "minecraft:cute",
                    "minecraft:grumpy",
                    "minecraft:puglin",
                    "minecraft:sad"
            };

            writeVarInt(pBuf, variants.length);
            for (String variant : variants) {
                writeString(pBuf, variant);
                pBuf.writeBoolean(false); // Подгрузить NBT локально из core
            }

            sendPacket(ctx, pBuf);
//            System.out.println("Sent " + variants.length + " wolf sound variants.");
        } finally {
            // Буфер гарантированно удалится и при успехе, и при ошибке
            if (pBuf != null && pBuf.refCnt() > 0) {
                pBuf.release();
            }
        }
    }

    public void sendChickenVariants(ChannelHandlerContext ctx) {
        ByteBuf pBuf = ctx.alloc().buffer();
        try {
            writeVarInt(pBuf, 0x07);
            writeString(pBuf, "minecraft:chicken_variant");

            String[] variants = {
                    "minecraft:cold",
                    "minecraft:temperate",
                    "minecraft:warm"
            };

            writeVarInt(pBuf, variants.length);
            for (String variant : variants) {
                writeString(pBuf, variant);
                pBuf.writeBoolean(false);
            }

            sendPacket(ctx, pBuf);
//            System.out.println("Sent " + variants.length + " chicken variants.");
        } finally {
            // Буфер гарантированно удалится и при успехе, и при ошибке
            if (pBuf != null && pBuf.refCnt() > 0) {
                pBuf.release();
            }
        }
    }

    public void sendCowVariants(ChannelHandlerContext ctx) {
        ByteBuf pBuf = ctx.alloc().buffer();
        try {
            writeVarInt(pBuf, 0x07);
            writeString(pBuf, "minecraft:cow_variant");

            String[] variants = {
                    "minecraft:cold",
                    "minecraft:temperate",
                    "minecraft:warm"
            };

            writeVarInt(pBuf, variants.length);
            for (String variant : variants) {
                writeString(pBuf, variant);
                pBuf.writeBoolean(false);
            }

            sendPacket(ctx, pBuf);
//            System.out.println("Sent " + variants.length + " cow variants.");
        } finally {
            // Буфер гарантированно удалится и при успехе, и при ошибке
            if (pBuf != null && pBuf.refCnt() > 0) {
                pBuf.release();
            }
        }
    }

    public void sendFrogVariants(ChannelHandlerContext ctx) {
        ByteBuf pBuf = ctx.alloc().buffer();
        try {
            writeVarInt(pBuf, 0x07); // Packet ID: registry_data
            writeString(pBuf, "minecraft:frog_variant");

            String[] variants = {
                    "minecraft:cold",
                    "minecraft:temperate",
                    "minecraft:warm"
            };

            writeVarInt(pBuf, variants.length);
            for (String variant : variants) {
                writeString(pBuf, variant);
                pBuf.writeBoolean(false); // NBT берется локально из core
            }

            sendPacket(ctx, pBuf);
//            System.out.println("Sent " + variants.length + " frog variants.");
        } finally {
            // Буфер гарантированно удалится и при успехе, и при ошибке
            if (pBuf != null && pBuf.refCnt() > 0) {
                pBuf.release();
            }
        }
    }

    public void sendPigVariants(ChannelHandlerContext ctx) {
        ByteBuf pBuf = ctx.alloc().buffer();
        try {
            writeVarInt(pBuf, 0x07);
            writeString(pBuf, "minecraft:pig_variant");

            String[] variants = {
                    "minecraft:cold",
                    "minecraft:temperate",
                    "minecraft:warm"
            };

            writeVarInt(pBuf, variants.length);
            for (String variant : variants) {
                writeString(pBuf, variant);
                pBuf.writeBoolean(false);
            }

            sendPacket(ctx, pBuf);
//            System.out.println("Sent " + variants.length + " pig variants.");
        } finally {
            // Буфер гарантированно удалится и при успехе, и при ошибке
            if (pBuf != null && pBuf.refCnt() > 0) {
                pBuf.release();
            }
        }
    }

    public void sendPaintingVariants(ChannelHandlerContext ctx) {
        ByteBuf pBuf = ctx.alloc().buffer();
        try {
            // 1. ID пакета Registry Data (Configuration)
            writeVarInt(pBuf, 0x07);

            // 2. Идентификатор реестра
            writeString(pBuf, "minecraft:painting_variant");

            // Полный список всех 51 ванильных вариантов картин
            String[] variants = {
                    "minecraft:alban", "minecraft:aztec", "minecraft:aztec2", "minecraft:backyard",
                    "minecraft:baroque", "minecraft:bomb", "minecraft:bouquet", "minecraft:burning_skull",
                    "minecraft:bust", "minecraft:cavebird", "minecraft:changing", "minecraft:cotan",
                    "minecraft:courbet", "minecraft:creebet", "minecraft:dennis", "minecraft:donkey_kong",
                    "minecraft:earth", "minecraft:endboss", "minecraft:fern", "minecraft:fighters",
                    "minecraft:finding", "minecraft:fire", "minecraft:graham", "minecraft:humble",
                    "minecraft:kebab", "minecraft:lowmist", "minecraft:match", "minecraft:meditative",
                    "minecraft:orb", "minecraft:owlemons", "minecraft:passage", "minecraft:pigscene",
                    "minecraft:plant", "minecraft:pointer", "minecraft:pond", "minecraft:pool",
                    "minecraft:prairie_ride", "minecraft:sea", "minecraft:skeleton", "minecraft:skull_and_roses",
                    "minecraft:stage", "minecraft:sunflowers", "minecraft:sunset", "minecraft:tides",
                    "minecraft:unpacked", "minecraft:void", "minecraft:wanderer", "minecraft:wasteland",
                    "minecraft:water", "minecraft:wind", "minecraft:wither"
            };

            // 3. Записываем количество элементов
            writeVarInt(pBuf, variants.length);

            for (String variant : variants) {
                // Имя элемента (Identifier)
                writeString(pBuf, variant);

                // Флаг "Has Data" = false (Клиент сам вытащит параметры ширины/высоты из minecraft:core)
                pBuf.writeBoolean(false);
            }

            // Отправляем пакет в сокет
            sendPacket(ctx, pBuf);
//            System.out.println("Sent " + variants.length + " painting variants successfully.");

        } finally {
            // Буфер гарантированно удалится и при успехе, и при ошибке
            if (pBuf != null && pBuf.refCnt() > 0) {
                pBuf.release();
            }
        }
    }

    public void sendDamageTypes(ChannelHandlerContext ctx) {
        ByteBuf pBuf = ctx.alloc().buffer();
        try {
            // 1. ID пакета Registry Data (Configuration)
            writeVarInt(pBuf, 0x07);

            // 2. Идентификатор реестра
            writeString(pBuf, "minecraft:damage_type");

            // Полный точный список из 49 ванильных типов урона
            String[] types = {
                    "minecraft:arrow", "minecraft:bad_respawn_point", "minecraft:cactus", "minecraft:campfire",
                    "minecraft:cramming", "minecraft:dragon_breath", "minecraft:drown", "minecraft:dry_out",
                    "minecraft:ender_pearl", "minecraft:explosion", "minecraft:fall", "minecraft:falling_anvil",
                    "minecraft:falling_block", "minecraft:falling_stalactite", "minecraft:fireball", "minecraft:fireworks",
                    "minecraft:fly_into_wall", "minecraft:freeze", "minecraft:generic", "minecraft:generic_kill",
                    "minecraft:hot_floor", "minecraft:in_fire", "minecraft:in_wall", "minecraft:indirect_magic",
                    "minecraft:lava", "minecraft:lightning_bolt", "minecraft:mace_smash", "minecraft:magic",
                    "minecraft:mob_attack", "minecraft:mob_attack_no_aggro", "minecraft:mob_projectile", "minecraft:on_fire",
                    "minecraft:out_of_world", "minecraft:outside_border", "minecraft:player_attack", "minecraft:player_explosion",
                    "minecraft:sonic_boom", "minecraft:spit", "minecraft:stalagmite", "minecraft:starve",
                    "minecraft:sting", "minecraft:sweet_berry_bush", "minecraft:thorns", "minecraft:thrown",
                    "minecraft:trident", "minecraft:unattributed_fireball", "minecraft:wind_charge", "minecraft:wither",
                    "minecraft:wither_skull"
            };

            // 3. Записываем количество элементов
            writeVarInt(pBuf, types.length);

            for (String type : types) {
                // Имя элемента (Identifier)
                writeString(pBuf, type);

                // Данные не передаем (NBT = false), клиент стянет параметры из core
                pBuf.writeBoolean(false);
            }

            sendPacket(ctx, pBuf);
//            System.out.println("Sent " + types.length + " damage types successfully.");

        } finally {
            // Буфер гарантированно удалится и при успехе, и при ошибке
            if (pBuf != null && pBuf.refCnt() > 0) {
                pBuf.release();
            }
        }
    }

    // Для NBT строк используем short (2 байта) длину
    private void writeNBTString(ByteBuf buf, String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        buf.writeShort(bytes.length);
        buf.writeBytes(bytes);
    }

    private void writeString(ByteBuf buf, String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        writeVarInt(buf, bytes.length);
        buf.writeBytes(bytes);
    }

    // Для ResourceLocation в пакетах Minecraft - VarInt длина!
    private void writeResourceLocation(ByteBuf buf, String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        writeVarInt(buf, bytes.length);
        buf.writeBytes(bytes);
    }

    // Для NBT строк - short (2 байта) длина!

    private void writeVarInt(ByteBuf buf, int value) {
        while ((value & 0xFFFFFF80) != 0) {
            buf.writeByte((value & 0x7F) | 0x80);
            value >>>= 7;
        }
        buf.writeByte(value & 0x7F);
    }

    private void sendPacket(ChannelHandlerContext ctx, ByteBuf buf) {
        ByteBuf finalBuf = ctx.alloc().buffer();
        writeVarInt(finalBuf, buf.readableBytes());
        finalBuf.writeBytes(buf);
        ctx.writeAndFlush(finalBuf);
    }
}