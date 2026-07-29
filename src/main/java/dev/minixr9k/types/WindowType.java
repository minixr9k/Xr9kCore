package dev.minixr9k.types;

public enum WindowType {
    GENERIC_9X1(0),
    GENERIC_9X2(1),
    GENERIC_9X3(2),
    GENERIC_9X4(3),
    GENERIC_9X5(4),
    GENERIC_9X6(5);

    private final int id;
    WindowType(int id) { this.id = id; }
    public int getId() { return id; }
}
