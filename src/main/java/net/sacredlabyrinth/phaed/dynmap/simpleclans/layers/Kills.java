package net.sacredlabyrinth.phaed.dynmap.simpleclans.layers;

import net.sacredlabyrinth.phaed.dynmap.simpleclans.DynmapSimpleClans;
import net.sacredlabyrinth.phaed.dynmap.simpleclans.Helper;
import net.sacredlabyrinth.phaed.dynmap.simpleclans.entries.KillEntry;
import net.sacredlabyrinth.phaed.simpleclans.Clan;
import org.bukkit.Location;
import org.bukkit.World;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class Kills {

	private static final String MARKER_SET = "simpleclans.deaths";
	private static final String ICON_ID = "simpleclans.death";
	private static final String ICON = "blood.png";
	private static final String CONFIG = "layer.kills.";
	private static final String LABEL = "Kills";
	private static final String FORMAT = "{vtag}&f{victim}|&7(killed by: {atag}&7{attacker}&7)";
	private final DynmapSimpleClans plugin;
	private boolean stop;
	private int task;
	private boolean enable;
	private int updateSeconds;
	private String label;
	private String format;
	private int layerPriority;
	private boolean hideByDefault;
	private int minZoom;
	private int visibleSeconds;
	private boolean clanDeaths;
	private boolean civilianDeaths;
	private MarkerSet markerSet;
	private MarkerIcon icon;
	private Map<String, Marker> markers = new HashMap<>();
	private final List<KillEntry> kills = new LinkedList<>();

	public Kills() {
		plugin = DynmapSimpleClans.getInstance();
		readConfig();

		if (enable) {
			initMarkerSet();
			initIcon();
			scheduleNextUpdate(5);
		}
	}

	private void readConfig() {
		enable = plugin.getConfig().getBoolean(CONFIG + "enable", true);
		updateSeconds = Math.max(plugin.getConfig().getInt(CONFIG + "update-seconds", 300), 2);
		label = plugin.getConfig().getString(CONFIG + "label", LABEL);
		format = plugin.getConfig().getString(CONFIG + "format", FORMAT);
		layerPriority = plugin.getConfig().getInt(CONFIG + "layer-priority", 10);
		hideByDefault = plugin.getConfig().getBoolean(CONFIG + "hide-by-default", false);
		minZoom = Math.max(plugin.getConfig().getInt(CONFIG + "min-zoom", 0), 0);

		visibleSeconds = Math.max(plugin.getConfig().getInt(CONFIG + "visible-seconds", 120), 2);
		clanDeaths = plugin.getConfig().getBoolean(CONFIG + "show.clan-players", true);
		civilianDeaths = plugin.getConfig().getBoolean(CONFIG + "show.civilians", true);
	}

	private void initMarkerSet() {
		markerSet = plugin.getMarkerApi().getMarkerSet(MARKER_SET);

		if (markerSet == null) {
			markerSet = plugin.getMarkerApi().createMarkerSet(MARKER_SET, label, null, false);
		} else {
			markerSet.setMarkerSetLabel(label);
		}

		if (markerSet == null) {
			DynmapSimpleClans.severe("Error creating " + LABEL + " marker set");
			return;
		}

		markerSet.setLayerPriority(layerPriority);
		markerSet.setHideByDefault(hideByDefault);
		markerSet.setMinZoom(minZoom);
	}

	private void initIcon() {
		icon = plugin.getMarkerApi().getMarkerIcon(ICON_ID);
		if (icon != null)
			icon.deleteIcon();
		InputStream stream;
		try {
			File bloodIcon = new File(plugin.getDataFolder(), "/images/" + ICON);
			stream = new FileInputStream(bloodIcon);
			icon = plugin.getMarkerApi().createMarkerIcon(ICON_ID, ICON_ID, stream);
			stream.close();
		} catch (IOException ex) {
			DynmapSimpleClans.severe("blood.png not found, using the one in the jar");
			stream = DynmapSimpleClans.class.getResourceAsStream("/images/" + ICON);
			icon = plugin.getMarkerApi().createMarkerIcon(ICON_ID, ICON_ID, stream);
		}

		if (icon == null) {
			DynmapSimpleClans.severe("Error creating icon");
		}
	}

	private void scheduleNextUpdate(int seconds) {
		if (!enable) {
			return;
		}
		plugin.getServer().getScheduler().cancelTask(task);
		task = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Update(), seconds * 20L);
	}

	private class Update implements Runnable {

		@Override
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

	private void updateMarkerSet() {
		cleanOldKills();

		Map<String, Marker> newMarkers = new HashMap<>();

		// get kills
		for (KillEntry kill : kills) {
			if (kill.getVictim().getClan() == null && !civilianDeaths) {
				continue;
			}
			if (kill.getVictim().getClan() != null && !clanDeaths) {
				continue;
			}

			String id = kill.toString();
			Location loc = kill.getLocation();
			World world = loc.getWorld();
			if (world == null) continue;
			String worldName = world.getName();
			// expand the label format

			Clan vclan = kill.getVictim().getClan();
			Clan aclan = kill.getVictim().getClan();
			String label = formatLabel(kill, vclan, aclan);

			// pull out the markers from the old set to reuse them

			Marker m = markers.remove(id);

			if (m == null) {
				m = markerSet.createMarker(id, label, true, worldName, loc.getX(), loc.getY(), loc.getZ(), icon,
						false);
			} else {
				m.setLocation(worldName, loc.getX(), loc.getY(), loc.getZ());
				m.setLabel(label, true);
				m.setMarkerIcon(icon);
			}

			newMarkers.put(id, m);
		}

		// delete all markers that we will no longer use
		markers.values().removeIf(Objects::isNull);
		for (Marker oldMarker : markers.values()) {
			if (oldMarker.getMarkerSet().getMarkers().contains(oldMarker)) {
				oldMarker.deleteMarker();
			}
		}

		// clean and replace the marker set
		markers.clear();
		markers = newMarkers;
	}

	private String formatLabel(KillEntry kill, Clan vclan, Clan aclan) {
		String label = format.replace("{victim}", kill.getVictim().getName())
				.replace("{attacker}", kill.getAttacker().getName())
				.replace("{vtag}", vclan == null ? "" : vclan.getTag())
				.replace("{atag}", aclan == null ? "" : aclan.getTag());
		return Helper.colorToHTML(label);
	}

	public void addKillEntry(KillEntry kill) {
		kills.add(kill);
		scheduleNextUpdate(1);
	}

	private void cleanOldKills() {
		kills.removeIf(killEntry -> killEntry.getAgeSeconds() > visibleSeconds);
	}
}
