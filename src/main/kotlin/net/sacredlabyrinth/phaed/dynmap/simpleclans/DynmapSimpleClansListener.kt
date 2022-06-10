package net.sacredlabyrinth.phaed.dynmap.simpleclans

import net.sacredlabyrinth.phaed.simpleclans.Clan
import net.sacredlabyrinth.phaed.simpleclans.Kill
import net.sacredlabyrinth.phaed.simpleclans.events.*
import org.bukkit.Bukkit.getScheduler
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority.MONITOR
import org.bukkit.event.Listener
import java.time.LocalDateTime

class DynmapSimpleClansListener(val plugin: DynmapSimpleClans) : Listener {

    @EventHandler(priority = MONITOR, ignoreCancelled = true)
    fun onSetHome(event: PlayerHomeSetEvent) = upsertMarkers(event.clan)

    @EventHandler(priority = MONITOR, ignoreCancelled = true)
    fun onHomeClear(event: PlayerHomeClearEvent) = deleteMarkers(event.clan.tag)

    @EventHandler(priority = MONITOR, ignoreCancelled = true)
    fun onDisband(event: DisbandClanEvent) = deleteMarkers(event.clan.tag)

    @EventHandler(priority = MONITOR, ignoreCancelled = true)
    fun onModtag(event: TagChangeEvent) = upsertMarkers(event.clan)

    @EventHandler(priority = MONITOR, ignoreCancelled = true)
    fun onKill(event: AddKillEvent) =
        plugin.killsLayer?.createMarker(Kill(event.attacker, event.victim, LocalDateTime.now()))

    private fun deleteMarkers(clanTag: String) {
        plugin.homeLayer?.markerSet?.findMarker(clanTag)?.deleteMarker()
        plugin.landsLayer?.markerSet?.findAreaMarker(clanTag)?.deleteMarker()
    }

    private fun upsertMarkers(clan: Clan) {
        // Running the runnable on next tick to get the actual clan data
        getScheduler().runTask(plugin, Runnable {
            plugin.homeLayer?.upsertMarker(clan)
            plugin.landsLayer?.upsertMarker(clan)
        })
    }
}

