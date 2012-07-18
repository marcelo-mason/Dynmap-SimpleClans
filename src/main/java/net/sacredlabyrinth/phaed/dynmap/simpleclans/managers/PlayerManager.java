package net.sacredlabyrinth.phaed.dynmap.simpleclans.managers;

import java.util.HashMap;
import java.util.Map;
import net.sacredlabyrinth.phaed.dynmap.simpleclans.DynmapSimpleClans;
import net.sacredlabyrinth.phaed.dynmap.simpleclans.entries.PlayerEntry;
import org.bukkit.entity.Player;

public class PlayerManager
{

    DynmapSimpleClans plugin;
    private Map<String, PlayerEntry> players = new HashMap<String, PlayerEntry>();

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
