package net.sacredlabyrinth.phaed.dynmap.simpleclans;

import java.util.List;

import org.bukkit.World;
import org.bukkit.entity.Player;

import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;

public class Toggles
{

    private final String CONFIG = "toggles" + ".";
    private DynmapSimpleClans plugin;
    private boolean stop;
    private int updateSeconds;
    private boolean hideWarring;

    public Toggles()
    {
        plugin = DynmapSimpleClans.getInstance();
        readConfig();

        if (hideWarring) {
            scheduleNextUpdate(5);
        }
    }

    private void readConfig()
    {
        updateSeconds = Math.max(plugin.getConfig().getInt(CONFIG + "update-seconds", 5), 2);
        hideWarring = plugin.getConfig().getBoolean(CONFIG + "hide-warring", true);
    }

    private void scheduleNextUpdate(int seconds)
    {
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Update(), seconds * 20);
    }

    private class Update implements Runnable
    {

        public void run()
        {
            if (!stop) {
                updateWarring();
                scheduleNextUpdate(updateSeconds);
            }
        }
    }

    public void cleanup()
    {
        stop = true;
    }

    private void updateWarring()
    {
        // hide all players that are in war

        if (hideWarring) {
            for (World world : plugin.getServer().getWorlds()) {
                List<Player> players = world.getPlayers();

                for (Player player : players) {
                    ClanPlayer clanPlayer = plugin.getClanManager().getClanPlayer(player);

                    if (clanPlayer == null) {
                        continue;
                    }

                    if (!clanPlayer.getClan().getWarringClans().isEmpty()) {
                        plugin.getDynmapApi().assertPlayerInvisibility(player, true, plugin);
                    } else {
                        plugin.getDynmapApi().assertPlayerInvisibility(player, false, plugin);
                    }
                }
            }
        }
    }
}
