package dev.minixr9k;

import dev.minixr9k.config.Configuration;
import dev.minixr9k.config.PluginLoader;
import dev.minixr9k.features.World;

public class MiniCore {

    public static void main(String[] args) {
        Configuration.loadConfig();
        PluginLoader.loadPlugins();
        World.initWorld(Configuration.get().world.chunks, Configuration.get().world.grassChunks);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("[ChunkHolder] Выключение сервера... Сохраняем мир...");
            World.saveWorld();
        }));
        new NetworkServer(Configuration.get().port).start();
    }

}
