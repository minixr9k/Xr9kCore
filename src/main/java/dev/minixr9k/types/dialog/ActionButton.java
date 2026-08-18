package dev.minixr9k.types.dialog;

public class ActionButton {

    private String text = "";
    private int width = 150;
    private String customAction = "";

    public ActionButton(String text) {
        this.text = text;
    }

    public ActionButton(String text, int width) {
        this.text = text;
        this.width = width;
    }

    public ActionButton(String text, int width, String customAction) {
        this.text = text;
        this.width = width;
        this.customAction = customAction;
    }

    public String getText() {
        return text;
    }

    public int getWidth() {
        return width;
    }

    public String getCustomAction() {
        return customAction;
    }
}
