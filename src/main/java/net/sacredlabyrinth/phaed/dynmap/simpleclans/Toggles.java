package net.sacredlabyrinth.phaed.dynmap.simpleclans;

import com.p000ison.dev.simpleclans2.clanplayer.ClanPlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.List;

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
        updateSeconds = Math.max(plugin.getCfg().getInt(CONFIG + "update-seconds", 5), 2);
        hideWarring = plugin.getCfg().getBoolean(CONFIG + "hide-warring", true);
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
        if (!hideWarring) {
            return;
        }

        // hide all players that are in war

        boolean hide = plugin.getCfg().getBoolean(CONFIG + "hide-warring", true);

        if (hide) {
            for (World world : plugin.getServer().getWorlds()) {
                List<Player> players = world.getPlayers();

                for (Player player : players) {
                    ClanPlayer clanPlayer = plugin.getClaPlayernManager().getClanPlayer(player);

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
