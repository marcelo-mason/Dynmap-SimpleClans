package net.sacredlabyrinth.phaed.dynmap.simpleclans

import net.sacredlabyrinth.phaed.simpleclans.Kill
import net.sacredlabyrinth.phaed.simpleclans.events.AddKillEvent
import net.sacredlabyrinth.phaed.simpleclans.events.DisbandClanEvent
import net.sacredlabyrinth.phaed.simpleclans.events.PlayerHomeSetEvent
import net.sacredlabyrinth.phaed.simpleclans.events.TagChangeEvent
import org.bukkit.Bukkit.getScheduler
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority.MONITOR
import org.bukkit.event.Listener
import java.time.LocalDateTime

class DynmapSimpleClansListener(val plugin: DynmapSimpleClans) : Listener {

    @EventHandler(priority = MONITOR, ignoreCancelled = true)
    fun onSetHome(event: PlayerHomeSetEvent) {
        val clan = event.clan
        // Running the runnable on next tick to get the actual clan home location
        getScheduler().runTask(plugin, Runnable {
            plugin.homeLayer?.upsertMarker(clan)
            plugin.landsLayer?.upsertMarker(clan)
        })
    }

    @EventHandler(priority = MONITOR)
    fun onDisband(event: DisbandClanEvent) {
        val tag = event.clan.tag
        plugin.homeLayer?.markerSet?.findMarker(tag)?.deleteMarker()
        plugin.landsLayer?.markerSet?.findMarker(tag)?.deleteMarker()
    }

    @EventHandler
    fun onModtag(event: TagChangeEvent) {
        // Running the runnable on next tick to get the actual clan tag color
        getScheduler().runTask(plugin, Runnable {
            plugin.landsLayer?.upsertMarker(event.clan)
        })
    }

    @EventHandler(priority = MONITOR)
    fun onKill(event: AddKillEvent) =
        plugin.killsLayer?.createMarker(Kill(event.attacker, event.victim, LocalDateTime.now()))
}
