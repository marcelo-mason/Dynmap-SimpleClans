package net.sacredlabyrinth.phaed.dynmap.simpleclans

import net.sacredlabyrinth.phaed.simpleclans.Kill
import net.sacredlabyrinth.phaed.simpleclans.events.AddKillEvent
import net.sacredlabyrinth.phaed.simpleclans.events.PlayerHomeSetEvent
import org.bukkit.Bukkit.getScheduler
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority.MONITOR
import org.bukkit.event.Listener
import java.time.LocalDateTime

class DynmapSimpleClansListener(val plugin: DynmapSimpleClans) : Listener {

    @EventHandler(priority = MONITOR, ignoreCancelled = true)
    fun onSetHome(event: PlayerHomeSetEvent) {
        // Running the runnable on next tick to get the actual clan home location
        getScheduler().runTask(plugin, Runnable { plugin.homeLayer?.upsertMarker(event.clan) })
    }

    @EventHandler(priority = MONITOR)
    fun onKill(event: AddKillEvent) =
        plugin.killsLayer?.createMarker(Kill(event.attacker, event.victim, LocalDateTime.now()))
}
