package dev.minixr9k.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.minixr9k.json.JConfig;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Configuration {
    public static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static JConfig config;

    public static void loadConfig() {
        try (FileReader reader = new FileReader("configuration.json")) {

            config = gson.fromJson(reader, JConfig.class);
            System.out.println("Конфиг успещно загружен!");

        } catch (FileNotFoundException e) {
            System.out.println("Конфиг не найден! Создаем новый...");
            config = new JConfig();
            saveConfig();
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при чтении конфигурации", e);
        }
    }

    public static void saveConfig() {
        if (config == null) {
            config = new JConfig();
        }

        try (FileWriter writer = new FileWriter("configuration.json")) {
            gson.toJson(config, writer);
            System.out.println("Конфиг создан!");
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при сохранении конфигурации", e);
        }
    }

    public static JConfig get() {
        return config;
    }
}
