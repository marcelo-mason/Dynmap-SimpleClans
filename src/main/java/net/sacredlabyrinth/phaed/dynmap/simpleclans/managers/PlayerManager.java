package net.sacredlabyrinth.phaed.dynmap.simpleclans.managers;

import java.util.HashMap;
import java.util.Map;
import net.sacredlabyrinth.phaed.dynmap.simpleclans.DynmapSimpleClans;
import net.sacredlabyrinth.phaed.dynmap.simpleclans.entries.PlayerEntry;
import org.bukkit.entity.Player;

public class PlayerManager
{

	private final String CONFIG = "players" + ".";
    DynmapSimpleClans plugin;
    private Map<String, PlayerEntry> players = new HashMap<String, PlayerEntry>();
	private Boolean hidePlayersByDefault;

    public PlayerManager()
    {
    	plugin = DynmapSimpleClans.getInstance();
    	readConfig();
    }

	private void readConfig()
	{
		hidePlayersByDefault = plugin.getCfg().getBoolean(CONFIG + "hide-by-default", true);
	}

    public void addEntry(Player player)
    {
        if (!players.containsKey(player.getName())) {
            players.put(player.getName(), new PlayerEntry(player, !hidePlayersByDefault));
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
