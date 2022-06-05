package net.sacredlabyrinth.phaed.dynmap.simpleclans

import net.sacredlabyrinth.phaed.simpleclans.Kill
import net.sacredlabyrinth.phaed.simpleclans.events.AddKillEvent
import net.sacredlabyrinth.phaed.simpleclans.events.PlayerHomeSetEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority.MONITOR
import org.bukkit.event.Listener
import org.bukkit.scheduler.BukkitRunnable
import java.time.LocalDateTime


class DynmapSimpleClansListener(val plugin: DynmapSimpleClans) : Listener {

    @EventHandler(priority = MONITOR, ignoreCancelled = true)
    fun onSetHome(event: PlayerHomeSetEvent) {
        // Running on next tick to get the actual clan home location
        object : BukkitRunnable() {
            override fun run() {
                plugin.homeLayer?.upsertMarker(event.clan)
            }
        }.runTask(plugin)
    }

    @EventHandler(priority = MONITOR)
    fun onKill(event: AddKillEvent) =
        plugin.killsLayer?.createMarker(Kill(event.attacker, event.victim, LocalDateTime.now()))
}