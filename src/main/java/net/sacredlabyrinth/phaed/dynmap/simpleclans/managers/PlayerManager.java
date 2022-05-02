package net.sacredlabyrinth.phaed.dynmap.simpleclans.managers;

import net.sacredlabyrinth.phaed.dynmap.simpleclans.DynmapSimpleClans;
import net.sacredlabyrinth.phaed.dynmap.simpleclans.entries.PlayerEntry;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class PlayerManager
{

    DynmapSimpleClans plugin;
    private final Map<String, PlayerEntry> players = new HashMap<>();

    public PlayerManager()
    {
        plugin = DynmapSimpleClans.getInstance();
    }

    public void addEntry(Player player)
    {
        if (!players.containsKey(player.getName())) {
            players.put(player.getName(), new PlayerEntry(player));
        }
    }

    public PlayerEntry getEntry(Player player)
    {
        if (!players.containsKey(player.getName())) {
            addEntry(player);
        }

        return players.get(player.getName());
    }
}
