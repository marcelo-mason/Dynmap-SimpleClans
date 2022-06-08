package net.sacredlabyrinth.phaed.dynmap.simpleclans;

import net.sacredlabyrinth.phaed.dynmap.simpleclans.layers.HomesLayer;
import net.sacredlabyrinth.phaed.dynmap.simpleclans.layers.KillsLayer;
import net.sacredlabyrinth.phaed.dynmap.simpleclans.layers.LandsLayer;
import net.sacredlabyrinth.phaed.dynmap.simpleclans.layers.LayerConfig;
import net.sacredlabyrinth.phaed.dynmap.simpleclans.managers.CommandManager;
import net.sacredlabyrinth.phaed.dynmap.simpleclans.tasks.HideWarringClansTask;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;
import net.sacredlabyrinth.phaed.simpleclans.managers.ClanManager;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.MarkerAPI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Objects;

import static org.bukkit.Bukkit.getPluginManager;
import static org.bukkit.Bukkit.getScheduler;

public class DynmapSimpleClans extends JavaPlugin {
    private static DynmapSimpleClans instance;
    private DynmapAPI dynmapApi;
    private MarkerAPI markerApi;
    private SimpleClans simpleclans;
    private @Nullable HomesLayer homesLayer;
    private @Nullable KillsLayer killsLayer;
    private LandsLayer landsLayer;
    private FileConfiguration configuration;

    /**
     * @return The plugin instance
     */
    public static DynmapSimpleClans getInstance() {
        return instance;
    }

    /**
     * @param messageKey The key
     * @return Translated colored message from key
     */
    public static String lang(@NotNull String messageKey) {
        String msg = instance.getConfiguration().getString("language." + messageKey);
        return msg == null ?
                String.format("Missing language for %s key", messageKey) :
                ChatColor.translateAlternateColorCodes('&', msg);
    }

    /**
     * Sends a debug message to the console, respecting the user decision.
     *
     * @param message             message to send
     * @param respectUserDecision should the message be sent if debug is false?
     */
    public static void debug(String message, boolean respectUserDecision) {
        if (respectUserDecision && !instance.getConfiguration().getBoolean("debug", false)) {
            return;
        }

        instance.getLogger().info(message);
    }

    public static void debug(String message) {
        debug(message, true);
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        reload();
        new CommandManager(this);
        getPluginManager().registerEvents(new DynmapSimpleClansListener(this), this);
    }

    public FileConfiguration getConfiguration() {
        return configuration;
    }

    public void reload() {
        reloadConfig();
        configuration = getConfig();

        loadDependencies();
        saveDefaultImages(configuration);
        loadTasks(configuration);
        loadLayers(configuration);
    }

    public void loadLayers(FileConfiguration config) {
        ConfigurationSection clanHomesSection = Objects.requireNonNull(config.getConfigurationSection("layer.homes"));
        ConfigurationSection killsSection = Objects.requireNonNull(config.getConfigurationSection("layer.kills"));
        ConfigurationSection landsSection = Objects.requireNonNull(config.getConfigurationSection("layer.lands"));

        String defaultHomeIcon = clanHomesSection.getString("default-icon", DefaultIcons.CLANHOME.getName());

        IconStorage homesIcons = new IconStorage(this, "/images/clanhome", defaultHomeIcon, markerApi);
        IconStorage killsIcons = new IconStorage(this, "/images", DefaultIcons.BLOOD.getName(), markerApi);

        try {
            homesLayer = new HomesLayer(getClanManager(), homesIcons, new LayerConfig(clanHomesSection), markerApi);
        } catch (IllegalStateException ex) {
            debug(ex.getMessage());
        }

        try {
            killsLayer = new KillsLayer(killsIcons, new LayerConfig(killsSection), markerApi);
        } catch (IllegalStateException ex) {
            debug(ex.getMessage());
        }

        try {
            // Running on next ticks to safely receive lands coordinates
            getScheduler().runTask(this, () ->
                    landsLayer = new LandsLayer(getClanManager(), simpleclans.getProtectionManager(), new LayerConfig(landsSection), markerApi));
        } catch (IllegalStateException ex) {
            debug(ex.getMessage());
        }
    }

    @NotNull
    public ClanManager getClanManager() {
        return simpleclans.getClanManager();
    }

    @NotNull
    public DynmapAPI getDynmapApi() {
        return dynmapApi;
    }

    @Nullable
    public HomesLayer getHomeLayer() {
        return homesLayer;
    }

    @Nullable
    public KillsLayer getKillsLayer() {
        return killsLayer;
    }

    @Nullable
    public LandsLayer getLandsLayer() {
        return landsLayer;
    }
    
    private void loadDependencies() {
        dynmapApi = (DynmapAPI) getServer().getPluginManager().getPlugin("DynMap");
        simpleclans = SimpleClans.getInstance();

        if (simpleclans == null) {
            getLogger().severe("SimpleClans wasn't found, disabling...");
            getPluginLoader().disablePlugin(this);
        }

        if (dynmapApi == null) {
            getLogger().severe("Dynmap wasn't found, disabling...");
            getPluginLoader().disablePlugin(this);
        }

        markerApi = dynmapApi.getMarkerAPI();
        if (markerApi == null) {
            getLogger().severe("'markers' component has not been configured in DynMap! Disabling...");
            getPluginLoader().disablePlugin(this);
        }
    }

    private void saveDefaultImages(FileConfiguration config) {
        String clanhomePath = DefaultIcons.CLANHOME.getPath();
        String bloodPath = DefaultIcons.BLOOD.getPath();

        boolean usedDefaultIcon = config.getString("layer.homes.default-icon", "clanhome").
                equalsIgnoreCase(DefaultIcons.CLANHOME.getName());

        if (!new File(getDataFolder(), clanhomePath).exists() && usedDefaultIcon) {
            saveResource(clanhomePath, false);
        }
        if (!new File(getDataFolder(), bloodPath).exists()) {
            saveResource(bloodPath, false);
        }
    }

    private void loadTasks(FileConfiguration config) {
        if (!config.getBoolean("hide-warring-players", true)) {
            new HideWarringClansTask(this);
        }
    }

    public enum DefaultIcons {
        CLANHOME("clanhome", "images/clanhome/clanhome.png"),
        BLOOD("blood", "images/blood.png");

        private final String name;
        private final String path;

        DefaultIcons(String name, String path) {
            this.name = name;
            this.path = path;
        }

        public String getPath() {
            return path;
        }

        public String getName() {
            return name;
        }
    }
}
