package dev.minixr9k.types.bossbar;

import dev.minixr9k.packets.play.ClientboundBossbar;
import dev.minixr9k.types.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BossBar {

    private final UUID uuid;
    private String title;
    private float progress;
    private BarColor color;
    private BarStyle style;

    private byte flags;

    private final List<Player> playerList = new ArrayList<>();

    public BossBar(String title, BarColor color, BarStyle style) {
        this.uuid = UUID.randomUUID();
        this.title = title;
        this.color = color;
        this.progress = 1f;
        this.style = style;
        this.flags = 0;
    }

    public BossBar(String title, BarColor color, BarStyle style, int progress) {
        this.uuid = UUID.randomUUID();
        this.title = title;
        this.color = color;
        this.progress = progress;
        this.style = style;
        this.flags = 0;
    }

    public BossBar(UUID uuid, String title, BarColor color, BarStyle style, int progress) {
        this.uuid = uuid;
        this.title = title;
        this.color = color;
        this.progress = progress;
        this.style = style;
        this.flags = 0;
    }

    public enum BossBarAction {
        ADD(0),
        REMOVE(1),
        UPDATE_HEALTH(2),
        UPDATE_TITLE(3),
        UPDATE_STYLE(4),
        UPDATE_FLAGS(5);

        private final int id;
        BossBarAction(int id) { this.id = id; }
        public int getId() { return id; }
    }

    public void setProgress(float progress) {
        this.progress = progress;
        for (Player player : playerList) {
            new ClientboundBossbar(this, BossBarAction.UPDATE_HEALTH).send(player.getCtx(), player.getProtocolVersion());
        }
    }

    public void setTitle(String title) {
        this.title = title;
        for (Player player : playerList) {
            new ClientboundBossbar(this, BossBarAction.UPDATE_TITLE).send(player.getCtx(), player.getProtocolVersion());
        }
    }

    public void setColor(BarColor color) {
        this.color = color;
        for (Player player : playerList) {
            new ClientboundBossbar(this, BossBarAction.UPDATE_STYLE).send(player.getCtx(), player.getProtocolVersion());
        }
    }

    public void setStyle(BarStyle style) {
        this.style = style;
        for (Player player : playerList) {
            new ClientboundBossbar(this, BossBarAction.UPDATE_STYLE).send(player.getCtx(), player.getProtocolVersion());
        }
    }

    public void setFlags(byte flags) {
        this.flags = flags;
        for (Player player : playerList) {
            new ClientboundBossbar(this, BossBarAction.UPDATE_FLAGS).send(player.getCtx(), player.getProtocolVersion());
        }
    }

    public void addPlayer(Player player) {
        playerList.add(player);
        new ClientboundBossbar(this, BossBarAction.ADD).send(player.getCtx(), player.getProtocolVersion());
    }

    public void removePlayer(Player player) {
        playerList.remove(player);
        new ClientboundBossbar(this, BossBarAction.REMOVE).send(player.getCtx(), player.getProtocolVersion());
    }

    public void removeAll() {
        for (Player player : playerList) {
            new ClientboundBossbar(this, BossBarAction.REMOVE).send(player.getCtx(), player.getProtocolVersion());
        }
        playerList.clear();
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getTitle() {
        return title;
    }

    public float getProgress() {
        return progress;
    }

    public BarColor getColor() {
        return color;
    }

    public BarStyle getStyle() {
        return style;
    }

    public byte getFlags() {
        return flags;
    }

    public List<Player> getPlayerList() {
        return playerList;
    }



}
