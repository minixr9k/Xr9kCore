package dev.minixr9k.types;

import dev.minixr9k.packets.play.ClientboundSetPlayerSlot;
import io.netty.channel.ChannelHandlerContext;

import java.util.Arrays;

public class Inventory {

    private final ChannelHandlerContext ctx;
    private final int protocolVersion;

    public Inventory(ChannelHandlerContext ctx, int protocolVersion) {
        this.ctx = ctx;
        this.protocolVersion = protocolVersion;
    }

    private final ItemStack[] slots = new ItemStack[46];

    private ItemStack carriedItem;

    private short activeSlot = 0;

    public ItemStack getItem(int slot) {
        if (slot < 0 || slot > slots.length) return null;
        return slots[slot];
    }

    public ItemStack getItemHotbar(int slot) {
        if (slot < 0 || slot > slots.length) return null;
        return slots[slot + 36];
    }

    public ItemStack getItemInMainHand() {
        return getItem(activeSlot + 36);
    }

    public void setItemInMainHand(ItemStack item) {
        setItemHotbar(activeSlot, item);
    }

    public void setItemHotbar(int slot, ItemStack item) {
        if (slot < 0 || slot > slots.length) return;
        slots[slot + 36] = item;
        new ClientboundSetPlayerSlot(slot, item).send(ctx, protocolVersion);
    }

    public void setItem(int slot, ItemStack item) {
        if (slot < 0 || slot > slots.length) return;
        slots[slot] = item;
    }

    public void clear() {
        Arrays.fill(slots, null);
    }

    public ItemStack[] getSlots() {
        return slots;
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

    public void setHelmet(ItemStack item) {
        setItem(5, item);
    }

    public void setChestplate(ItemStack item) {
        setItem(6, item);
    }

    public void setLeggings(ItemStack item) {
        setItem(7, item);
    }

    public void setBoots(ItemStack item) {
        setItem(8, item);
    }

    public ItemStack getHelmet() {
        return getItem(5);
    }

    public ItemStack getChestplate() {
        return getItem(6);
    }

    public ItemStack getLeggings() {
        return getItem(7);
    }

    public ItemStack getBoots() {
        return getItem(8);
    }
}
