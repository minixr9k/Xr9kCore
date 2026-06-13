package dev.minixr9k;

import dev.minixr9k.registries.SchematicRegistry;

public class Xr9kCore {

    public static void main(String[] args) throws Exception {
        SchematicRegistry.load();
        new NetworkServer(25565).start();
    }

}
