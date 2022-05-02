package net.sacredlabyrinth.phaed.dynmap.simpleclans;

import net.sacredlabyrinth.phaed.dynmap.simpleclans.layers.ClanHomes;
import net.sacredlabyrinth.phaed.dynmap.simpleclans.layers.Kills;
import net.sacredlabyrinth.phaed.dynmap.simpleclans.managers.CommandManager;
import net.sacredlabyrinth.phaed.dynmap.simpleclans.managers.PlayerManager;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;
import net.sacredlabyrinth.phaed.simpleclans.managers.ClanManager;
import net.sacredlabyrinth.phaed.simpleclans.managers.SettingsManager;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.MarkerAPI;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Objects;
import java.util.logging.Logger;

import static org.bukkit.Bukkit.getPluginManager;

public class DynmapSimpleClans extends JavaPlugin {
	private static DynmapSimpleClans instance;
	public static final Logger logger = Logger.getLogger("Dynmap-SimpleClans");
	private DynmapAPI dynmapApi;
	private MarkerAPI markerApi;
	private SimpleClans simpleclans;

	private PlayerManager playerManager;
	private ClanHomes clanHomes;
	private Kills kills;
	private Toggles toggles;

	@Override
	public void onEnable() {
		instance = this;
		logger.info("initializing");

		dynmapApi = (DynmapAPI) getServer().getPluginManager().getPlugin("DynMap");
		simpleclans = (SimpleClans) getServer().getPluginManager().getPlugin("SimpleClans");

		playerManager = new PlayerManager();
		CommandManager commandManager = new CommandManager();

		activate();

		getPluginManager().registerEvents(new DynmapSimpleClansListener(this), this);
		Objects.requireNonNull(getCommand("clanmap")).setExecutor(commandManager);
	}

	public void activate() {
		initApis();

		reloadConfig();
		saveDefaultConfig();
		// saving images
		saveDefaultImages();

		// set up layers

		clanHomes = new ClanHomes();
		toggles = new Toggles();
		kills = new Kills();

		logger.info("version " + this.getDescription().getVersion() + " is activated");
	}

	public void saveDefaultImages() {
		if (!new File(getDataFolder(), "/images/clanhome").exists()) {
			saveResource("images/clanhome/clanhome.png", false);
		}
		if (!new File(getDataFolder(), "/images/blood.png").exists()) {
			saveResource("images/blood.png", false);
		}
	}

	@Override
	public void onDisable() {
		cleanup();
	}

	public void cleanup() {
		if (clanHomes != null) {
			clanHomes.cleanup();
		}
		if (toggles != null) {
			toggles.cleanup();
		}
		if (kills != null) {
			kills.cleanup();
		}
	}

    private void initApis() {
        markerApi = dynmapApi.getMarkerAPI();

        if (markerApi == null) {
			logger.severe("Error loading Dynmap marker API!");
        }
    }

    public static DynmapSimpleClans getInstance() {
        return instance;
    }

    public MarkerAPI getMarkerApi() {
        return markerApi;
    }

    public ClanManager getClanManager() {
		return simpleclans.getClanManager();
	}

	public DynmapAPI getDynmapApi() {
		return dynmapApi;
    }

    public ClanHomes getClanHomes() {
        return clanHomes;
    }

    public Kills getKills() {
        return kills;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public SettingsManager getSettingsManager() {
        return simpleclans.getSettingsManager();
    }

    /**
     * @param messageKey The key
     * @return Translated colored message from key
     */
    public String getLang(@NotNull String messageKey) {
        String msg = getConfig().getString("language." + messageKey);
        return msg == null ?
                String.format("Missing language for %s key", messageKey) :
                ChatColor.translateAlternateColorCodes('&', msg);
    }
}
