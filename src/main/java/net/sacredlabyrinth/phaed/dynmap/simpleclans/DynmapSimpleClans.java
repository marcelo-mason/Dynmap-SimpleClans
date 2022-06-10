package net.sacredlabyrinth.phaed.dynmap.simpleclans;

import net.sacredlabyrinth.phaed.dynmap.simpleclans.IconStorage.DefaultIcons;
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
        String msg = instance.getConfig().getString("language." + messageKey);
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
        if (respectUserDecision && !instance.getConfig().getBoolean("debug", false)) {
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
        if (reload()) {
            loadTasks();
            new CommandManager(this);
            getPluginManager().registerEvents(new DynmapSimpleClansListener(this), this);
        }
    }

    public boolean reload() {
        reloadConfig();

        try {
            loadDependencies();
        } catch (IllegalStateException ex) {
            getLogger().severe(ex.getMessage());
            return false;
        }

        saveDefaultImages();
        loadLayers();

        return true;
    }

    public void loadLayers() {
        ConfigurationSection clanHomesSection = Objects.requireNonNull(getConfig().getConfigurationSection("layer.homes"));
        ConfigurationSection killsSection = Objects.requireNonNull(getConfig().getConfigurationSection("layer.kills"));
        ConfigurationSection landsSection = Objects.requireNonNull(getConfig().getConfigurationSection("layer.lands"));

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
        dynmapApi = (DynmapAPI) getPluginManager().getPlugin("DynMap");
        simpleclans = (SimpleClans) getPluginManager().getPlugin("SimpleClans");

        if (simpleclans == null || dynmapApi == null) {
            getPluginLoader().disablePlugin(this);
        }

        if (simpleclans == null) {
            throw new IllegalStateException("SimpleClans wasn't found, disabling...");
        }

        if (dynmapApi == null) {
            throw new IllegalStateException("Dynmap wasn't found, disabling...");
        }
        markerApi = dynmapApi.getMarkerAPI();
        if (markerApi == null) {
            getPluginLoader().disablePlugin(this);
            throw new IllegalStateException("'markers' component has not been configured in DynMap! Disabling...");
        }
    }

    private void saveDefaultImages() {
        String clanhomePath = DefaultIcons.CLANHOME.getPath();
        String bloodPath = DefaultIcons.BLOOD.getPath();

        boolean usedDefaultIcon = getConfig().getString("layer.homes.default-icon", "clanhome").
                equalsIgnoreCase(DefaultIcons.CLANHOME.getName());

        if (!new File(getDataFolder(), clanhomePath).exists() && usedDefaultIcon) {
            saveResource(clanhomePath, false);
        }
        if (!new File(getDataFolder(), bloodPath).exists()) {
            saveResource(bloodPath, false);
        }
    }

    private void loadTasks() {
        if (!getConfig().getBoolean("hide-warring-players", true)) {
            new HideWarringClansTask(this);
        }
    }
}
