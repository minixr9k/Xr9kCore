package dev.minixr9k.types;

public class Block {

    private final String type;
    private final Location location;

    public Block(String type, Location location) {
        this.type = type;
        this.location = location;
    }

    public String getType() {
        return type;
    }

    public Location getLocation() {
        return location;
    }
}
