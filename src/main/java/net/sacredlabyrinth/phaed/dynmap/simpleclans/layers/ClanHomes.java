package net.sacredlabyrinth.phaed.dynmap.simpleclans.layers;

import net.sacredlabyrinth.phaed.dynmap.simpleclans.DynmapSimpleClans;
import net.sacredlabyrinth.phaed.dynmap.simpleclans.Helper;
import net.sacredlabyrinth.phaed.dynmap.simpleclans.managers.PreferencesManager;
import net.sacredlabyrinth.phaed.simpleclans.Clan;
import net.sacredlabyrinth.phaed.simpleclans.managers.SettingsManager.ConfigField;
import org.bukkit.Location;
import org.bukkit.World;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClanHomes {

	private static final String MARKER_SET = "simpleclans.homes";
	private static final String CONFIG = "layer.homes.";
	private static final String LABEL = "Clan Homes";
	private static final String FORMAT = "{clan} &8(home)";
	private final DynmapSimpleClans plugin;
	private boolean stop;
	private int task;
	private boolean enable;
	private int updateSeconds;
	private String defaultIcon;
	private String label;
	private String format;
	private int layerPriority;
	private boolean hideByDefault;
	private int minZoom;
	List<String> hidden;
	Map<String, MarkerIcon> icons = new HashMap<>();
	private MarkerSet markerSet;
	private Map<String, Marker> markers = new HashMap<>();

	public ClanHomes() {
		plugin = DynmapSimpleClans.getInstance();
		readConfig();

		if (enable) {
			initMarkerSet();
			initIcons();
			scheduleNextUpdate(5);
		}
	}

	private void readConfig() {
		enable = plugin.getConfig().getBoolean(CONFIG + "enable", true);
		updateSeconds = Math.max(plugin.getConfig().getInt(CONFIG + "update-seconds", 300), 2);
		label = plugin.getConfig().getString(CONFIG + "label", LABEL);
		format = plugin.getConfig().getString(CONFIG + "format", FORMAT);
		layerPriority = plugin.getConfig().getInt(CONFIG + "layer-priority", 1);
		hideByDefault = plugin.getConfig().getBoolean(CONFIG + "hide-by-default", false);
		minZoom = Math.max(plugin.getConfig().getInt(CONFIG + "min-zoom", 0), 0);
		defaultIcon = plugin.getConfig().getString(CONFIG + "default-icon", "clanhome");
		hidden = plugin.getConfig().getStringList(CONFIG + "hidden-markers");
	}

	private void initMarkerSet() {
		markerSet = plugin.getMarkerApi().getMarkerSet(MARKER_SET);

		if (markerSet == null) {
			markerSet = plugin.getMarkerApi().createMarkerSet(MARKER_SET, label, null, false);
		} else {
			markerSet.setMarkerSetLabel(label);
		}

		if (markerSet == null) {
			DynmapSimpleClans.logger.severe("Error creating " + LABEL + " marker set");
			return;
		}

		markerSet.setLayerPriority(layerPriority);
		markerSet.setHideByDefault(hideByDefault);
		markerSet.setMinZoom(minZoom);
	}

	public Set<String> getIcons() {
		return icons.keySet();
	}

	private void initIcons() {
		MarkerAPI markerApi = plugin.getMarkerApi();
		File iconFolder = new File(plugin.getDataFolder(), "/images/clanhome");

		for (File i : iconFolder.listFiles(file -> file.getName().contains(".png"))) {
			try {
				String name = i.getName().split(".png")[0].toLowerCase();
				MarkerIcon icon = markerApi.getMarkerIcon("simpleclans_" + name);
				if (icon != null) {
					icon.deleteIcon();
				}
				FileInputStream stream = new FileInputStream(i);
				icon = markerApi.createMarkerIcon("simpleclans_" + name, "simpleclans_" + name, stream);
				if (icon == null) {
					DynmapSimpleClans.logger.severe("Error creating icon for " + i.getName());
				} else {
					icons.put(name, icon);
				}
				stream.close();
			} catch (IOException ignored) {
				DynmapSimpleClans.logger.severe("Error creating icon for " + i.getName());
			}
		}

		if (icons.isEmpty() || !icons.containsKey(defaultIcon)) {
			MarkerIcon icon = markerApi.getMarkerIcon("simpleclans_" + defaultIcon);
			if (icon != null)
				icon.deleteIcon();

			DynmapSimpleClans.logger.severe("clanhome.png not found, using the one in the jar");
			
			icon = markerApi.createMarkerIcon("simpleclans_" + defaultIcon, "simpleclans_" + defaultIcon,
					DynmapSimpleClans.class.getResourceAsStream("/images/clanhome/" + "clanhome.png"));
			icons.put(defaultIcon, icon);
		}
	}

	private void scheduleNextUpdate(int seconds) {
		plugin.getServer().getScheduler().cancelTask(task);
		task = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Update(), seconds * 20L);
	}

	private class Update implements Runnable {

		public void run() {
			if (!stop) {
				updateMarkerSet();
				scheduleNextUpdate(updateSeconds);
			}
		}
	}

	public void cleanup() {
		if (markerSet != null) {
			markerSet.deleteMarkerSet();
			markerSet = null;
		}

		markers.clear();
		stop = true;
	}

	private boolean isVisible(String id, String worldName) {
		if (hidden != null && !hidden.isEmpty()) {
			return !hidden.contains(id) && !hidden.contains("world:" + worldName);
		}
		return true;
	}

	private void updateMarkerSet() {
		Map<String, Marker> newMarkers = new HashMap<>();

		// get clans with homes

		List<Clan> clans = plugin.getClanManager().getClans();

		for (Clan clan : clans) {
			String id = clan.getTag();
			Location loc = clan.getHomeLocation();

			if (loc == null) {
				continue;
			}

			// skip if not visible

			World world = clan.getHomeLocation().getWorld();
			if (!isVisible(id, world.getName())) {
				continue;
			}

			// expand the label format
            String inactive = clan.getInactiveDays() + "/" + (clan.isVerified() ?
					plugin.getSettingsManager().getInt(ConfigField.PURGE_INACTIVE_CLAN_DAYS) :
					plugin.getSettingsManager().getInt(ConfigField.PURGE_UNVERIFIED_CLAN_DAYS));
            String membersOnline = net.sacredlabyrinth.phaed.simpleclans.Helper.stripOffLinePlayers(clan.getMembers()).size() + "/" + clan.getSize();
            String status = (clan.isVerified() ? plugin.getLang("verified") : plugin.getLang("unverified"));
            String feeEnabled = (clan.isMemberFeeEnabled() ? plugin.getLang("fee-enabled") : plugin.getLang("fee-disabled"));

			String label = format
					.replace("{clan}", clan.getName())
					.replace("{tag}", clan.getTag())
					.replace("{member_count}", String.valueOf(clan.getMembers().size()))
					.replace("{inactive}", inactive)
					.replace("{founded}", clan.getFoundedString())
					.replace("{rival}", String.valueOf(clan.getTotalRival()))
					.replace("{neutral}", String.valueOf(clan.getTotalNeutral()))
					.replace("{deaths}", String.valueOf(clan.getTotalDeaths()))
					.replace("{kdr}", String.valueOf(clan.getTotalKDR()))
					.replace("{civilian}", String.valueOf(clan.getTotalCivilian()))
					.replace("{members_online}", membersOnline)
					.replace("{leaders}", clan.getLeadersString("", ", "))
					.replace("{allies}", clan.getAllyString(", "))
					.replace("{rivals}", clan.getRivalString(", "))
					.replace("{fee_value}", String.valueOf(clan.getMemberFee()))
					.replace("{status}", status)
					.replace("{fee_enabled}", feeEnabled);
			
			label = Helper.colorToHTML(label);

			// pull out the markers from the old set to reuse them

			Marker m = markers.remove(id);

			PreferencesManager pm = new PreferencesManager(clan);
			String iconName = pm.getClanHomeIcon();
			if (iconName == null || !icons.containsKey(iconName)) {
				iconName = defaultIcon;
			}
			MarkerIcon icon = icons.get(iconName);

			if (m == null) {
				m = markerSet.createMarker(id, label, true, world.getName(), loc.getX(), loc.getY(), loc.getZ(), icon,
						false);
			} else {
				m.setLocation(world.getName(), loc.getX(), loc.getY(), loc.getZ());
				m.setLabel(label, true);
				m.setMarkerIcon(icon);
			}

			newMarkers.put(id, m);
		}

		// delete all markers that we will no longer use

		for (Marker oldMarker : markers.values()) {
			oldMarker.deleteMarker();
		}

		// clean and replace the marker set
		markers.clear();
		markers = newMarkers;
	}
}
