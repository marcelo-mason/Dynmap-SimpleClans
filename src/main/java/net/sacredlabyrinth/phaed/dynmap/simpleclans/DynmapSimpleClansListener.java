package net.sacredlabyrinth.phaed.dynmap.simpleclans;

import com.p000ison.dev.simpleclans2.clanplayer.ClanPlayer;
import net.sacredlabyrinth.phaed.dynmap.simpleclans.entries.KillEntry;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.PluginEnableEvent;

public class DynmapSimpleClansListener implements Listener
{
    private DynmapSimpleClans plugin;

    public DynmapSimpleClansListener()
    {
        plugin = DynmapSimpleClans.getInstance();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPluginEnable(PluginEnableEvent event)
    {
        String name = event.getPlugin().getDescription().getName();

        if (name.equals("dynmap") || name.equals("SimpleClans"))
        {
            plugin.activate();
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        plugin.getPlayerManager().addEntry(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onEntityDeath(EntityDeathEvent event)
    {
        if (event.getEntity() instanceof Player)
        {
            Player victim = (Player) event.getEntity();
            Player attacker = null;

            // find attacker

            EntityDamageEvent lastDamageCause = victim.getLastDamageCause();

            if (lastDamageCause instanceof EntityDamageByEntityEvent)
            {
                EntityDamageByEntityEvent entityEvent = (EntityDamageByEntityEvent) lastDamageCause;

                if (entityEvent.getDamager() instanceof Player)
                {
                    attacker = (Player) entityEvent.getDamager();
                }
                else if (entityEvent.getDamager() instanceof Arrow)
                {
                    Arrow arrow = (Arrow) entityEvent.getDamager();

                    if (arrow.getShooter() instanceof Player)
                    {
                        attacker = (Player) arrow.getShooter();
                    }
                }
            }

            if (attacker != null)
            {
                ClanPlayer acp = plugin.getClaPlayernManager().getCreateClanPlayerExact(attacker);
                ClanPlayer vcp = plugin.getClaPlayernManager().getCreateClanPlayerExact(victim);

                DynmapSimpleClans.getInstance().getKills().addKillEntry(new KillEntry(vcp, acp, victim.getLocation()));
            }
        }
    }
}