package dev.minixr9k.features;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class MiniMessage {

    public static String parse(String message) {
        // [{"text": "hello world", "color":"#ffffff"}]
        JsonArray jsonArray = new JsonArray();
        while (!message.isEmpty()) {
            JsonObject object = new JsonObject();
            String color = "";
            String coloredMessage = "";
            int startColor = message.indexOf("<");
            int endColor = message.indexOf(">");

            if (startColor == -1 && endColor == -1) {
                coloredMessage = message;
                message = "";
                object.addProperty("text", coloredMessage);
                jsonArray.add(object);
                continue;
            }

            color = message.substring(startColor + 1, endColor);
            message = message.substring(endColor + 1);
            int newColor = message.indexOf("<");
            if (newColor == -1) {
                coloredMessage = message;
                message = "";
            }
            else {
                coloredMessage = message.substring(0, newColor);
            }

            if (!coloredMessage.isEmpty())
                object.addProperty("text", coloredMessage);

            if (!coloredMessage.isEmpty() && !color.isEmpty())
                object.addProperty("color", color);

            jsonArray.add(object);
        }

        return jsonArray.toString();
    }

}
