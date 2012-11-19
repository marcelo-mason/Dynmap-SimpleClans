package net.sacredlabyrinth.phaed.dynmap.simpleclans.layers;

import com.p000ison.dev.simpleclans2.clan.Clan;
import net.sacredlabyrinth.phaed.dynmap.simpleclans.DynmapSimpleClans;
import net.sacredlabyrinth.phaed.dynmap.simpleclans.Helper;
import org.bukkit.Location;
import org.bukkit.World;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClanHomes
{

    private final String MARKER_SET = "simpleclans.homes";
    private final String ICON_ID = "simpleclans.home";
    private final String ICON = "clanhome.png";
    private final String CONFIG = "layer.homes.";
    private final String LABEL = "Clan Homes";
    private final String FORMAT = "{clan} &8(home)";
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
    List<String> hidden;
    private MarkerSet markerSet;
    private MarkerIcon icon;
    private Map<String, Marker> markers = new HashMap<String, Marker>();

    public ClanHomes()
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
        layerPriority = plugin.getCfg().getInt(CONFIG + "layer-priority", 1);
        hideByDefault = plugin.getCfg().getBoolean(CONFIG + "hide-by-default", false);
        minZoom = Math.max(plugin.getCfg().getInt(CONFIG + "min-zoom", 0), 0);

        hidden = plugin.getCfg().getStringList(CONFIG + "hidden-markers");
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

    private class Update implements Runnable
    {

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

    private boolean isVisible(String id, String worldName)
    {
        if (hidden != null && !hidden.isEmpty()) {
            if (hidden.contains(id) || hidden.contains("world:" + worldName)) {
                return false;
            }
        }
        return true;
    }

    private void updateMarkerSet()
    {
        Map<String, Marker> newMarkers = new HashMap<String, Marker>();

        // get clans with homes

        Set<Clan> clans = plugin.getClanManager().getClans();

        for (World world : plugin.getServer().getWorlds()) {
            for (Clan clan : clans) {
                String id = clan.getTag();
                Location loc = clan.getFlags().getHomeLocation();

                if (loc == null) {
                    continue;
                }

                // one world at a time

                if (!loc.getWorld().equals(world)) {
                    continue;
                }

                // skip if not visible

                if (!isVisible(id, world.getName())) {
                    continue;
                }

                // expand the label format

                String label = format;
                label = label.replace("{clan}", clan.getName());
                label = label.replace("{tag}", clan.getTag());
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
            oldMarker.deleteMarker();
        }

        // clean and replace the marker set

        markers.clear();
        markers = newMarkers;
    }
}
