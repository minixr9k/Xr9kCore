package dev.minixr9k.types;

public enum GameMode {
    ADVENTURE(2),
    CREATIVE(1),
    SURVIVAL(0),
    SPECTATOR(3);

    private final int id;
    GameMode(int id) {
        this.id = id;
    }
    public int getId() {
        return id;
    }
}
