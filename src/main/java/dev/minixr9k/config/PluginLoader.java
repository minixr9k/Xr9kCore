package dev.minixr9k.config;

import dev.minixr9k.api.Plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class PluginLoader {

    private static final List<Plugin> loadedPlugins = new ArrayList<>();

    public static void loadPlugins() {
        File folder = new File("plugins");
        if (!folder.exists()) folder.mkdir();

        File[] files = folder.listFiles();
        if (files == null) return;

        List<File> allJarFiles = new ArrayList<>();
        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(".jar")) {
                allJarFiles.add(file);
            }
        }

        System.out.println("[Core/PluginLoader] Найдено потенциальных плагинов: " + allJarFiles.size());
        if (allJarFiles.isEmpty()) return;

        for (File file : allJarFiles) {
            try (JarFile jar = new JarFile(file)) {
                JarEntry entry = jar.getJarEntry("plugin.yml");
                if (entry == null) {
                    System.out.printf("[Core/PluginLoader] %s не содержит plugin.yml!%n", file.getName());
                    continue;
                }

                // Читаем mainClass из plugin.yml
                String mainClassPath = null;
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(jar.getInputStream(entry)))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("mainClass:")) {
                            // Вырезаем сам путь класса, убирая пробелы
                            mainClassPath = line.substring("mainClass:".length()).trim();
                            break;
                        }
                    }
                }

                if (mainClassPath == null || mainClassPath.isEmpty()) {
                    System.out.printf("[Core/PluginLoader] %s: В plugin.yml не указан mainClass!%n", file.getName());
                    continue;
                }

                // --- ЗАГРУЗКА КЛАССА В ПАМЯТЬ ЯВЫ ---
                // Создаем загрузчик классов, передавая ему путь к .jar файлу
                URL[] urls = { file.toURI().toURL() };
                // Важно передать текущий ClassLoader ядра как родительский,
                // чтобы плагин видел классы твоего ядра (типа Entity, EventBus и т.д.)
                URLClassLoader classLoader = new URLClassLoader(urls, PluginLoader.class.getClassLoader());

                // Находим класс внутри джарника по его пути
                Class<?> clazz = classLoader.loadClass(mainClassPath);

                // Проверяем, реализует ли этот класс наш интерфейс Plugin
                if (!Plugin.class.isAssignableFrom(clazz)) {
                    System.out.printf("[Core/PluginLoader] Главный класс %s не реализует интерфейс Plugin!%n", mainClassPath);
                    classLoader.close();
                    continue;
                }

                // Создаем экземпляр (объект) плагина
                Plugin plugin = (Plugin) clazz.getDeclaredConstructor().newInstance();

                // Включаем плагин!
                plugin.onEnable();

                // Сохраняем в список активных
                loadedPlugins.add(plugin);
                System.out.printf("[Core/PluginLoader] Успешно загружен плагин %s%n", file.getName());

            } catch (Exception e) {
                System.out.printf("[Core/PluginLoader] Ошибка при загрузке плагина %s: %s%n", file.getName(), e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public static void disablePlugins() {
        System.out.println("[Core/PluginLoader] Выключение плагинов...");
        for (Plugin plugin : loadedPlugins) {
            try {
                plugin.onDisable();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        loadedPlugins.clear();
    }

}
