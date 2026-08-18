package dev.minixr9k.types.dialog;

public class PlainMessage {

    private String type = "minecraft:plain_message";
    private String contents = "";
    private int width = 200;

    public PlainMessage(String contents) {
        this.contents = contents;
    }

    public PlainMessage(String contents, int width) {
        this.contents = contents;
        this.width = width;
    }

    public String getType() {
        return type;
    }

    public String getContents() {
        return contents;
    }

    public int getWidth() {
        return width;
    }
}
