package dev.minixr9k.json;

import dev.minixr9k.types.ItemStack;

public class JInventory {

    private ItemStack[] slots = new ItemStack[46];

    private ItemStack carriedItem;
    private short activeSlot = 0;

    public ItemStack[] getSlots() {
        return slots;
    }

    public void setSlots(ItemStack[] slots) {
        this.slots = slots;
    }

    public short getActiveSlot() {
        return activeSlot;
    }

    public void setActiveSlot(short activeSlot) {
        this.activeSlot = activeSlot;
    }

    public ItemStack getCarriedItem() {
        return carriedItem;
    }

    public void setCarriedItem(ItemStack carriedItem) {
        this.carriedItem = carriedItem;
    }

}
