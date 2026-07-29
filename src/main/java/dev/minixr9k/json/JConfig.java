package dev.minixr9k.json;

import dev.minixr9k.types.GameMode;
import dev.minixr9k.types.Location;

import java.util.HashMap;
import java.util.Map;

public class JConfig {
    public int port = 25565;
    public GameMode gameMode = GameMode.SURVIVAL;
    public boolean forceGameMode = false;
    public int time = 8000;
    public boolean isTimeIncreasing = false;
    public boolean anticheat = true;
    public boolean buildInSkins = true;
    public MotdConf motd = new MotdConf();
    public WorldConf world = new WorldConf();
    public ResourcePackConf resourcepack = new ResourcePackConf();
    public BungeecordConf bungeecord = new BungeecordConf();
    public Location spawnPosition = new Location(0, 112, 0);
    public Map<String, Integer> operators = new HashMap<>();

    public static class ResourcePackConf {
        public String url = "";
        public String sha1 = "";
        public boolean forced = false;
        public String prompt = "";
    }

    public static class BungeecordConf {
        public boolean enabled = false;
        public String token = "";
    }

    public static class MotdConf {
        public String text = "A MiniCore server (1.21.8)";
        public int maxPlayers = -1;
    }

    public static class WorldConf {
        public String type = "FLAT";
        public int chunks = 8;
        public int grassChunks = 2;
        public int renderDistance = 12;
        public int simulationDistance = 12;
    }

    public static class FeaturesConf {
        public boolean fishing = true;
        public boolean worldVoid = true;
    }

}
