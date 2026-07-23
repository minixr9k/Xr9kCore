package dev.minixr9k;

import dev.minixr9k.config.PluginLoader;
import dev.minixr9k.features.World;

public class Xr9kCore {

    public static void main(String[] args) {
        PluginLoader.loadPlugins();
        World.initWorld(8, 2);

        // 2. Регистрируем автосохранение при выключении процесса
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("[LinearChunkHolder] Выключение сервера... Сохраняем мир...");
            World.saveWorld();
        }));
        new NetworkServer(25565).start();
    }

}
