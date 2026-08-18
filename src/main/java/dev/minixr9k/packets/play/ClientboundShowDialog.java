package dev.minixr9k.packets.play;

import dev.minixr9k.types.dialog.ActionButton;
import dev.minixr9k.types.dialog.Dialog;
import dev.minixr9k.types.dialog.DialogType;
import dev.minixr9k.types.dialog.PlainMessage;
import dev.minixr9k.utils.MinecraftPacket;
import io.netty.buffer.ByteBuf;

import static dev.minixr9k.utils.ProtocolUtils.*;

public class ClientboundShowDialog implements MinecraftPacket {

    private final Dialog dialog;

    public ClientboundShowDialog(Dialog dialog) {
        this.dialog = dialog;
    }

    @Override
    public void write(ByteBuf out, int protocolVersion) {

        writeVarInt(out, 0);

        out.writeByte(10); // tag compound

        out.writeByte(8); // tag string
        writeNBTString(out, "type");
        writeNBTString(out, "minecraft:" + dialog.getType().name().toLowerCase());

        out.writeByte(8); // tag string
        writeNBTString(out, "title");
        writeNBTString(out, dialog.getTitle());

        if (!dialog.getBody().isEmpty()) {
            out.writeByte(10);
            writeNBTString(out, "body");

            for (Object obj : dialog.getBody()) {
                if (obj instanceof PlainMessage element) {
                    out.writeByte(8); // tag string
                    writeNBTString(out, "type");
                    writeNBTString(out, element.getType());

                    out.writeByte(8); // tag string
                    writeNBTString(out, "contents");
                    writeNBTString(out, element.getContents());

                    if (element.getWidth() != 200) {
                        out.writeByte(3); // tag int
                        writeNBTString(out, "width");
                        out.writeInt(element.getWidth());
                    }
                }
            }

            out.writeByte(0); // tag end
        }

        if (dialog.getType() == DialogType.MULTI_ACTION) {
            out.writeByte(9);
            writeNBTString(out, "actions");
            out.writeByte(10);

            out.writeInt(dialog.getActions().size());

            for (Object obj : dialog.getActions()) {
                if (obj instanceof ActionButton element) {
                    out.writeByte(8); // tag string
                    writeNBTString(out, "label");
                    writeNBTString(out, element.getText());

                    if (element.getWidth() != 150) {
                        out.writeByte(3); // tag int
                        writeNBTString(out, "width");
                        out.writeInt(element.getWidth());
                    }

                    if (!element.getCustomAction().isEmpty()) {
                        out.writeByte(10);
                        writeNBTString(out, "action");

                        out.writeByte(8); // tag string
                        writeNBTString(out, "type");
                        writeNBTString(out, "custom");

                        out.writeByte(8); // tag string
                        writeNBTString(out, "id");
                        writeNBTString(out, element.getCustomAction());

                        out.writeByte(0);
                    }

                    out.writeByte(0);
                }
            }
        }

        if (dialog.getType() == DialogType.MULTI_ACTION
        || dialog.getType() == DialogType.SERVER_LINKS
        || dialog.getType() == DialogType.DIALOG_LIST) {
            if (dialog.getColumns() != 2) {
                out.writeByte(3); // tag int
                writeNBTString(out, "columns");
                out.writeInt(dialog.getColumns());
            }
        }

        if (!dialog.isCloseable()) {
            out.writeByte(1); // tag byte
            writeNBTString(out, "can_close_with_escape");
            out.writeBoolean(dialog.isCloseable());
        }


        out.writeByte(0); // tag end
    }

    @Override
    public void read(ByteBuf in, int protocolVersion) {

    }

    @Override
    public int getPacketId(int protocolVersion) {
        return 0x85;
    }
}
