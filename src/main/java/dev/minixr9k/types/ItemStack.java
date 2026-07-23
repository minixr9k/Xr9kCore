package dev.minixr9k.types;

import java.util.List;

public class ItemStack {
    private final String type;
    private final short count;
    private List<ItemComponent> components;

    public ItemStack(String type, short count) {
        this.type = type;
        this.count = count;
    }

    public ItemStack(String type, short count, List<ItemComponent> components) {
        this.type = type;
        this.count = count;
        this.components = components;
    }

    public String getType() {
        return type;
    }

    public short getCount() {
        return count;
    }

    public List<ItemComponent> getComponents() {
        return components;
    }
}
