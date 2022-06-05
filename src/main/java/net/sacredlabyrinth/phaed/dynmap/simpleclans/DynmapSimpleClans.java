package net.sacredlabyrinth.phaed.dynmap.simpleclans;

import net.sacredlabyrinth.phaed.dynmap.simpleclans.layers.HomeLayer;
import net.sacredlabyrinth.phaed.dynmap.simpleclans.layers.KillsLayer;
import net.sacredlabyrinth.phaed.dynmap.simpleclans.layers.LayerConfig;
import net.sacredlabyrinth.phaed.dynmap.simpleclans.managers.CommandManager;
import net.sacredlabyrinth.phaed.dynmap.simpleclans.tasks.HideWarringClansTask;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;
import net.sacredlabyrinth.phaed.simpleclans.managers.ClanManager;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.MarkerAPI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collections;
import java.util.Objects;
import java.util.logging.Level;

import static net.sacredlabyrinth.phaed.dynmap.simpleclans.Preferences.loadPreferences;
import static org.bukkit.Bukkit.getPluginManager;

public class DynmapSimpleClans extends JavaPlugin {
    private static DynmapSimpleClans instance;
    private DynmapAPI dynmapApi;
    private MarkerAPI markerApi;
    private SimpleClans simpleclans;
    private HomeLayer homeLayer;
    private KillsLayer killsLayer;

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

        reload();

        CommandManager commandManager = new CommandManager(this);
        PluginCommand clanmap = Objects.requireNonNull(getCommand("clanmap"));
        clanmap.setExecutor(commandManager);
        clanmap.setTabCompleter(commandManager);

        getPluginManager().registerEvents(new DynmapSimpleClansListener(this), this);

        getLogger().log(Level.INFO, "Version {0} is activated!", getDescription().getVersion());
    }

    public void reload() {
        loadPreferences();
        loadDependencies();
        loadTasks();
        saveDefaultConfig();
        saveDefaultImages();
        loadLayers();
    }

    public void loadLayers() {
        ConfigurationSection clanHomesSection = Objects.requireNonNull(getConfig().getConfigurationSection("layer.homes"));
        ConfigurationSection killsSection = Objects.requireNonNull(getConfig().getConfigurationSection("layer.kills"));

        String defaultIconName = clanHomesSection.getString("default-icon", "clanhome");

        IconStorage homeIcons = new IconStorage(this, "/images/clanhome", defaultIconName, markerApi);
        IconStorage killsIcons = new IconStorage(this, "/images", "blood", markerApi);

        try {
            homeLayer = new HomeLayer(homeIcons, new LayerConfig(clanHomesSection), markerApi);
            killsLayer = new KillsLayer(killsIcons, new LayerConfig(killsSection), markerApi);
        } catch (IllegalStateException ex) {
            debug(ex.getMessage());
        }
    }

    public ClanManager getClanManager() {
        return simpleclans.getClanManager();
    }

    public DynmapAPI getDynmapApi() {
        return dynmapApi;
    }

    @Nullable
    public HomeLayer getHomeLayer() {
        return homeLayer;
    }

    @Nullable
    public KillsLayer getKillsLayer() {
        return killsLayer;
    }


    private void loadDependencies() {
        dynmapApi = (DynmapAPI) getServer().getPluginManager().getPlugin("DynMap");
        if (dynmapApi != null) {
            markerApi = dynmapApi.getMarkerAPI();
            if (markerApi == null) {
                getLogger().severe("'markers' component has not been configured in DynMap!");
            }
        }

        simpleclans = SimpleClans.getInstance();
    }

    private void saveDefaultImages() {
        if (!new File(getDataFolder(), "/images/clanhome").exists()) {
            saveResource("images/clanhome/clanhome.png", false);
        }
        if (!new File(getDataFolder(), "/images/blood.png").exists()) {
            saveResource("images/blood.png", false);
        }
    }

    private void loadTasks() {
        if (!getConfig().getBoolean("hide-warring-players", true)) {
            new HideWarringClansTask(this);
        }
    }
}
