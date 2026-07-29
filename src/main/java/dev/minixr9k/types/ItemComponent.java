package dev.minixr9k.types;

public class ItemComponent {

    private final String componentType;
    private final Object componentValue;

    public ItemComponent() {
        this.componentType = "";
        this.componentValue = null;
    }

    public ItemComponent(String componentType) {
        this.componentType = componentType;
        this.componentValue = null;
    }

    public ItemComponent(String componentType, Object componentValue) {
        this.componentType = componentType;
        this.componentValue = componentValue;
    }

    public String getComponentType() {
        return componentType;
    }

    public Object getComponentValue() {
        return componentValue;
    }

    public int getValueAsInt() {
        if (componentValue instanceof Number) {
            return ((Number) componentValue).intValue();
        }
        return 0;
    }

    public float getValueAsFloat() {
        if (componentValue instanceof Number) {
            return ((Number) componentValue).floatValue();
        }
        return 0;
    }

    public String getValueAsString() {
        return componentValue != null ? componentValue.toString() : "";
    }
}
