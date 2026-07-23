package dev.minixr9k.types;

public class ItemComponent {

    private final String componentType;
    private final short componentValue;

    public ItemComponent() {
        this.componentType = "";
        this.componentValue = -1;
    }

    public ItemComponent(String componentType) {
        this.componentType = componentType;
        this.componentValue = -1;
    }

    public ItemComponent(String componentType, short componentValue) {
        this.componentType = componentType;
        this.componentValue = componentValue;
    }

    public String getComponentType() {
        return componentType;
    }

    public short getComponentValue() {
        return componentValue;
    }
}
