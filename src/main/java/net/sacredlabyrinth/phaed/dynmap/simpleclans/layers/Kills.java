package net.sacredlabyrinth.phaed.dynmap.simpleclans.layers;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.World;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;

import net.sacredlabyrinth.phaed.dynmap.simpleclans.DynmapSimpleClans;
import net.sacredlabyrinth.phaed.dynmap.simpleclans.Helper;
import net.sacredlabyrinth.phaed.dynmap.simpleclans.entries.KillEntry;
import net.sacredlabyrinth.phaed.simpleclans.Clan;

public class Kills {

    private final String MARKER_SET = "simpleclans.deaths";
    private final String ICON_ID = "simpleclans.death";
    private final String ICON = "blood.png";
    private final String CONFIG = "layer.kills.";
    private final String LABEL = "Kills";
    private final String FORMAT = "{vtag}&f{victim}|&7(killed by: {atag}&7{attacker}&7)";
    private DynmapSimpleClans plugin;
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
    private Map<String, Marker> markers = new HashMap<String, Marker>();
    private List<KillEntry> kills = new LinkedList<KillEntry>();

    public Kills()
    {
        plugin = DynmapSimpleClans.getInstance();
        readConfig();

        if (enable) {
            initMarkerSet();
            initIcon();
            scheduleNextUpdate(5);
        }
    }

    private void readConfig()
    {
        enable = plugin.getCfg().getBoolean(CONFIG + "enable", true);
        updateSeconds = Math.max(plugin.getCfg().getInt(CONFIG + "update-seconds", 300), 2);
        label = plugin.getCfg().getString(CONFIG + "label", LABEL);
        format = plugin.getCfg().getString(CONFIG + "format", FORMAT);
        layerPriority = plugin.getCfg().getInt(CONFIG + "layer-priority", 10);
        hideByDefault = plugin.getCfg().getBoolean(CONFIG + "hide-by-default", false);
        minZoom = Math.max(plugin.getCfg().getInt(CONFIG + "min-zoom", 0), 0);

        visibleSeconds = Math.max(plugin.getCfg().getInt(CONFIG + "visible-seconds", 120), 2);
        clanDeaths = plugin.getCfg().getBoolean(CONFIG + "show.clan-players", true);
        civilianDeaths = plugin.getCfg().getBoolean(CONFIG + "show.civilians", true);
    }

    private void initMarkerSet()
    {
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

    private void initIcon()
    {
        icon = plugin.getMarkerApi().getMarkerIcon(ICON_ID);

        if (icon == null) {
            InputStream stream = DynmapSimpleClans.class.getResourceAsStream("/images/" + ICON);
            icon = plugin.getMarkerApi().createMarkerIcon(ICON_ID, ICON_ID, stream);
        }

        if (icon == null) {
            DynmapSimpleClans.severe("Error creating icon");
        }
    }

    private void scheduleNextUpdate(int seconds)
    {
        plugin.getServer().getScheduler().cancelTask(task);
        task = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Update(), seconds * 20);
    }

    private class Update implements Runnable {

        public void run()
        {
            if (!stop) {
                updateMarkerSet();
                scheduleNextUpdate(updateSeconds);
            }
        }
    }

    public void cleanup()
    {
        if (markerSet != null) {
            markerSet.deleteMarkerSet();
            markerSet = null;
        }
        markers.clear();
        stop = true;
    }

    private void updateMarkerSet()
    {
        cleanOldKills();

        Map<String, Marker> newMarkers = new HashMap<String, Marker>();

        // get kills

        for (World world : plugin.getServer().getWorlds()) {
            for (KillEntry kill : kills) {
                String id = kill.toString();
                Location loc = kill.getLocation();

                // one world at a time

                if (loc.getWorld() != world) {
                    continue;
                }

                // expand the label format

                String label = format;
                Clan vclan = kill.getVictim().getClan();
                Clan aclan = kill.getVictim().getClan();
                label = label.replace("{victim}", kill.getVictim().getName());
                label = label.replace("{attacker}", kill.getAttacker().getName());
                label = label.replace("{vtag}", vclan == null ? "" : vclan.getTag());
                label = label.replace("{atag}", aclan == null ? "" : aclan.getTag());
                label = Helper.colorToHTML(label);

                // pull out the markers from the old set to reuse them

                Marker m = markers.remove(id);

                if (m == null) {
                    m = markerSet.createMarker(id, label, true, world.getName(), loc.getX(), loc.getY(), loc.getZ(), icon, false);
                } else {
                    m.setLocation(world.getName(), loc.getX(), loc.getY(), loc.getZ());
                    m.setLabel(label, true);
                    m.setMarkerIcon(icon);
                }

                newMarkers.put(id, m);
            }
        }

        // delete all markers that we will no longer use

        for (Marker oldMarker : markers.values()) {
            if (oldMarker.getMarkerSet().getMarkers().contains(oldMarker)) {
                oldMarker.deleteMarker();
            }
        }

        // clean and replace the marker set

        markers.clear();
        markers = newMarkers;
    }

    public void addKillEntry(KillEntry kill)
    {
        kills.add(kill);
        scheduleNextUpdate(1);
    }

    private void cleanOldKills()
    {
        for (Iterator iter = kills.iterator(); iter.hasNext(); ) {
            KillEntry kill = (KillEntry) iter.next();

            if (kill.getAgeSeconds() > visibleSeconds) {
                iter.remove();
            }
        }
    }
}
