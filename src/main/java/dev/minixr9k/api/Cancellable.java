package dev.minixr9k.api;

public interface Cancellable {

    void setCancelled(boolean cancel);
    boolean isCancelled();

}
