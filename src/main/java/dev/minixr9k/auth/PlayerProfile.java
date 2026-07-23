package dev.minixr9k.auth;

public class PlayerProfile {

    private final String name;
    private final String value;
    private final String signature;

    public PlayerProfile(String name, String value) {
        this(name, value, (String)null);
    }

    public PlayerProfile(String name, String value, String signature) {
        this.name = name;
        this.value = value;
        this.signature = signature;
    }

    public String getName() {
        return this.name;
    }

    public String getValue() {
        return this.value;
    }

    public boolean hasSignature() {
        return this.signature != null;
    }

    public String getSignature() {
        return this.signature;
    }

}
