package dev.minixr9k.types;

import dev.minixr9k.auth.PlayerProfile;
import dev.minixr9k.features.World;
import dev.minixr9k.packets.beta.play.ChatMessage3Packet;
import dev.minixr9k.packets.beta.play.PlayerPositionAndLook13Packet;
import dev.minixr9k.packets.beta.play.TimeUpdate4Packet;
import dev.minixr9k.packets.confAndPlay.ClientboundDisconnect;
import dev.minixr9k.packets.play.*;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;

public class Player extends Entity {

    private String username;
    private GameMode gameMode;
    private boolean isAllowFlight;
    private boolean isFlying;
    private boolean isLoaded;
    private int teleportImmunityTicks = 0;
    private int opLevel;
    private boolean isSprinting;
    private boolean isGliding;

    private int health = 20;
    private int food = 20;
    private int saturation = 20;

    private int passengerOfEntity = 0;

    private int ping;

    private List<PlayerProfile> profile;
    private String brand;

    private ChannelHandlerContext ctx;
    private int protocolVersion;

    public Player(ChannelHandlerContext ctx, int protocolVersion) {
        this.ctx = ctx;
        this.protocolVersion = protocolVersion;
        setInventory(new Inventory(ctx, protocolVersion));
    }

    public void sendMessage(String message) {
        if (protocolVersion <= 99)
            new ChatMessage3Packet(message).send(ctx, protocolVersion);
        else
            new ClientboundSystemMessage(message, false).send(ctx, protocolVersion);
    }

    public void sendActionBar(String message) {
        if (protocolVersion <= 99)
            new ChatMessage3Packet(message).send(ctx, protocolVersion);
        else
            new ClientboundSystemMessage(message, true).send(ctx, protocolVersion);
    }

    public void sendTitle(String title, String subtitle) {
        new ClientboundSetTitleText(title).send(ctx, protocolVersion);
        new ClientboundSetSubTitleText(subtitle).send(ctx, protocolVersion);
    }

    public void sendTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        new ClientboundSetTitleText(title).send(ctx, protocolVersion);
        new ClientboundSetSubTitleText(subtitle).send(ctx, protocolVersion);
        new ClientboundSetTitleAnimation(fadeIn, stay, fadeOut).send(ctx, protocolVersion);
    }

    public void clearTitle() {
        new ClientboundClearTitle(false).send(ctx, protocolVersion);
    }

    public void resetTitle() {
        new ClientboundClearTitle(true).send(ctx, protocolVersion);
    }

    public void playSound(String sound, float volume, float pitch) {
        new ClientboundSoundEntity(sound, 0, 1, volume, pitch, System.currentTimeMillis()).send(ctx, protocolVersion);
    }

    public void stopSound(String sound) {
        new ClientboundStopSound((byte) 2, -1, sound).send(ctx, protocolVersion);
    }

    public void stopAllSounds() {
        new ClientboundStopSound((byte) 0, -1, "").send(ctx, protocolVersion);
    }

    public void teleport(double x, double y, double z) {
        this.teleportImmunityTicks = 3;
        this.setX(x);
        this.setY(y);
        this.setZ(z);
        if (protocolVersion < 99) {
            new PlayerPositionAndLook13Packet(x, y, y + 1.62, z, 0, 0, false).send(ctx, protocolVersion);
            return;
        }
        new ClientboundSyncPlayerPos(0, x, y, z, 0, 0).send(ctx, protocolVersion);
    }

    public void teleport(Location location) {
        this.teleportImmunityTicks = 3;
        this.setX(location.getX());
        this.setY(location.getY());
        this.setZ(location.getZ());
        if (protocolVersion < 99) {
            new PlayerPositionAndLook13Packet(location.getX(), location.getY(), location.getY() + 1.62, location.getZ(), 0, 0, false).send(ctx, protocolVersion);
            return;
        }
        new ClientboundSyncPlayerPos(0, location.getX(), location.getY(), location.getZ(), 0, 0).send(ctx, protocolVersion);
    }

    public void teleport(double x, double y, double z, float yaw, float pitch) {
        this.teleportImmunityTicks = 3;
        this.setX(x);
        this.setY(y);
        this.setZ(z);
        this.setYaw(yaw);
        this.setPitch(pitch);
        if (protocolVersion < 99) {
            new PlayerPositionAndLook13Packet(x, y, y + 1.62, z, yaw, pitch, false).send(ctx, protocolVersion);
            return;
        }
        new ClientboundSyncPlayerPos(0, x, y, z, yaw, pitch).send(ctx, protocolVersion);
    }

    public void teleportBack(double x, double y, double z) {
        this.teleportImmunityTicks = 3;
        this.setX(x);
        this.setY(y);
        this.setZ(z);
        if (protocolVersion < 99) {
            new PlayerPositionAndLook13Packet(x, y, y + 1.62, z, 0, 0, false).send(ctx, protocolVersion);
            return;
        }
        new ClientboundSyncPlayerPos(0, x, y, z, 0, 0).send(ctx, protocolVersion);
    }

    public void teleportBack(double x, double y, double z, float yaw, float pitch) {
        this.teleportImmunityTicks = 3;
        this.setX(x);
        this.setY(y);
        this.setZ(z);
        if (protocolVersion < 99) {
            new PlayerPositionAndLook13Packet(x, y, y + 1.62, z, yaw, pitch, false).send(ctx, protocolVersion);
            return;
        }
        new ClientboundSyncPlayerPos(0, x, y, z, yaw, pitch).send(ctx, protocolVersion);
    }

    public void setGamemode(GameMode gamemode) {
        this.gameMode = gamemode;

        setAllowFlight(gamemode == GameMode.CREATIVE || gamemode == GameMode.SPECTATOR);

        new ClientboundGameEvent(3, gamemode.getId()).send(ctx, protocolVersion);
    }

    public void setVisualGamemode() {
        new ClientboundGameEvent(3, GameMode.CREATIVE.getId()).send(ctx, protocolVersion);
        setAllowFlight(true);
    }

    public void setHealth(int health) {
        if (health <= 0) {
            this.health = 20;
            new ClientboundSetHealth(20, food, saturation).send(ctx, protocolVersion);
            return;
        }
        this.health = health;
        new ClientboundSetHealth(health, food, saturation).send(ctx, protocolVersion);
    }

    public void setFoodLevel(int food) {
        this.food = food;
        new ClientboundSetHealth(health, food, saturation).send(ctx, protocolVersion);
    }

    public void setSaturation(int saturation) {
        this.saturation = saturation;
        new ClientboundSetHealth(health, food, saturation).send(ctx, protocolVersion);
    }

    public void setLocalBlock(String block, int x, int y, int z) {
        World.setLocalBlock(x, y, z, block, this);
    }

    public void setLocalWorldTime(int time, boolean isTimeIncreasing) {
        if (protocolVersion < 99) {
            new TimeUpdate4Packet(time).send(ctx, protocolVersion);
            return;
        }
        new ClientboundUpdateTime(time, time, isTimeIncreasing).send(ctx, protocolVersion);
    }

    public void sendTabList(String header, String footer) {
        new ClientboundTabList(header, footer).send(ctx, protocolVersion);
    }

    public void sendGameEvent(int eventId, int value) {
        new ClientboundGameEvent(eventId, value).send(ctx, protocolVersion);
    }

    public void sendPluginMessage(String channel, String command, String value) {
        // "bungeecord:main", "Connect", "auth"
        new ClientboundPluginMessage(channel, command, value).send(ctx, protocolVersion);
    }

    public void kick(String reason) {
        new ClientboundDisconnect(reason).send(ctx, protocolVersion);
        ctx.close();
    }

    public void setOpLevel(int opLevel) {
        this.opLevel = opLevel;
        switch (opLevel) {
            case 0:
                new ClientboundEntityEvent(1, (byte) 24).send(ctx, protocolVersion);
                break;
            case 1:
                new ClientboundEntityEvent(1, (byte) 25).send(ctx, protocolVersion);
                break;
            case 2:
                new ClientboundEntityEvent(1, (byte) 26).send(ctx, protocolVersion);
                break;
            case 3:
                new ClientboundEntityEvent(1, (byte) 27).send(ctx, protocolVersion);
                break;
            case 4:
                new ClientboundEntityEvent(1, (byte) 28).send(ctx, protocolVersion);
                break;
        }
    }

    public int getOpLevel() { return opLevel; }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setSystemGameMode(GameMode gameMode) { this.gameMode = gameMode; }

    public GameMode getGameMode() {
        return gameMode;
    }

    public boolean isAllowFlight() {
        return isAllowFlight;
    }

    public void setAllowFlight(boolean allowFlight) {
        isAllowFlight = allowFlight;
        if (allowFlight)
            new ClientboundPlayerAbilities((byte) 0x04, 0.05f, 0.1f).send(ctx, protocolVersion);
        else
            switch (this.gameMode) {
                case ADVENTURE -> {
                    new ClientboundGameEvent(3, 2).send(ctx, protocolVersion);
                }
                case CREATIVE -> {
                    new ClientboundGameEvent(3, 1).send(ctx, protocolVersion);
                }
                case SURVIVAL -> {
                    new ClientboundGameEvent(3, 0).send(ctx, protocolVersion);
                }
                case SPECTATOR -> {
                    new ClientboundGameEvent(3, 3).send(ctx, protocolVersion);
                }
            }
    }

    public boolean isFlying() {
        return isFlying;
    }

    public void setFlying(boolean flying) {
        isFlying = flying;
        if (flying)
            new ClientboundPlayerAbilities((byte) 0x02, 0.05f, 0.1f).send(ctx, protocolVersion);
        else
            switch (this.gameMode) {
                case ADVENTURE -> {
                    new ClientboundGameEvent(3, 2).send(ctx, protocolVersion);
                }
                case CREATIVE -> {
                    new ClientboundGameEvent(3, 1).send(ctx, protocolVersion);
                }
                case SURVIVAL -> {
                    new ClientboundGameEvent(3, 0).send(ctx, protocolVersion);
                }
                case SPECTATOR -> {
                    new ClientboundGameEvent(3, 3).send(ctx, protocolVersion);
                }
            }
    }

    public void updateInventory(int stateId) {
        new ClientboundContainerSetContent(0, stateId, getInventory().getSlots(), getInventory().getCarriedItem()).send(ctx, protocolVersion);
    }

    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    public void setCtx(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    public int getProtocolVersion() { return protocolVersion; }

    public void setProtocolVersion(int protocolVersion) { this.protocolVersion = protocolVersion; }

    public List<PlayerProfile> getProfile() { return profile; }

    public void setProfile(List<PlayerProfile> profile) { this.profile = profile; }

    public int getPassengerOfEntity() {
        return passengerOfEntity;
    }

    public void setPassengerOfEntity(int passengerOfEntity) {
        this.passengerOfEntity = passengerOfEntity;
    }

    public int getHealth() {
        return health;
    }

    public int getFood() {
        return food;
    }

    public int getSaturation() {
        return saturation;
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    public void setLoaded(boolean loaded) {
        isLoaded = loaded;
    }

    public int getTeleportImmunityTicks() {
        return teleportImmunityTicks;
    }

    public void decreaseImmunity() {
        if (this.teleportImmunityTicks > 0) {
            this.teleportImmunityTicks--;
        }
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public boolean isSprinting() {
        return isSprinting;
    }

    public void setSprinting(boolean sprinting) {
        isSprinting = sprinting;
    }

    public boolean isGliding() {
        return isGliding;
    }

    public void setGliding(boolean gliding) {
        isGliding = gliding;
    }

    public int getPing() {
        return ping;
    }

    public void setPing(int ping) {
        this.ping = ping;
    }

}
