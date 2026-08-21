package dev.minixr9k.json;

import dev.minixr9k.types.GameMode;
import dev.minixr9k.types.Location;

import java.util.HashMap;
import java.util.Map;

public class JConfig {
    public int port = 25565;
    public GameMode gameMode = GameMode.SURVIVAL;
    public boolean forceGameMode = false;
    public boolean hardcore = false;
    public int time = 8000;
    public boolean isTimeIncreasing = false;
    public boolean anticheat = true;
    public boolean buildInSkins = true;
    public MotdConf motd = new MotdConf();
    public WorldConf world = new WorldConf();
    public ResourcePackConf resourcepack = new ResourcePackConf();
    public ProxyConf proxy = new ProxyConf();
    public FeaturesConf features = new FeaturesConf();
    public Location spawnPosition = new Location(0, 112, 0);
    public Map<String, Integer> operators = new HashMap<>();

    public static class ResourcePackConf {
        public String url = "";
        public String sha1 = "";
        public boolean forced = false;
        public String prompt = "";
    }

    public static class ProxyConf {
        public boolean enabled = false;
        public ForwardingMode forwardingMode = ForwardingMode.BUNGEEGUARD;
        public String token = "";
    }

    public enum ForwardingMode {
        BUNGEEGUARD,
        MODERN
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
        public boolean flatType = true;
    }

    public static class FeaturesConf {
        public boolean acceptTransfers = true;
        public boolean worldVoid = true;
        public boolean debug = false;
        public boolean logCommands = true;
        public boolean logBrand = true;
        public boolean buildInCommands = true;
        public boolean betaSupport = false;
        public boolean lceSupport = false;
        public String chunkFormat = "DEV/ZREGION";
        public int mainProtocol = 772;
        public boolean fixBoatFly = true;
        public boolean illegalCharactersCheck = true;
        public boolean pingSameProtocol = true;
        public boolean buildInMessages = true;
    }

}
