package net.sacredlabyrinth.phaed.dynmap.simpleclans.layers

import net.sacredlabyrinth.phaed.dynmap.simpleclans.DynmapSimpleClans
import net.sacredlabyrinth.phaed.dynmap.simpleclans.Helper
import net.sacredlabyrinth.phaed.dynmap.simpleclans.IconStorage
import net.sacredlabyrinth.phaed.simpleclans.Kill
import org.bukkit.scheduler.BukkitRunnable
import org.dynmap.markers.MarkerAPI

class KillsLayer(iconStorage: IconStorage, config: LayerConfig, markerAPI: MarkerAPI) :
    Layer("simpleclans.layers.kill", iconStorage, config, markerAPI) {

    fun createMarker(kill: Kill) {
        val vclan = kill.victim.clan
        val victim = kill.victim.toPlayer() ?: return
        val loc = victim.location
        val worldName = loc.world?.name

        if (vclan == null && !config.section.getBoolean("show.clan-players", true) ||
            vclan != null && !config.section.getBoolean("show.civilians", true)) {
            return
        }

        val marker = markerSet.createMarker(
            kill.time.toString(), formatLabel(kill), true,
            worldName, loc.x, loc.y, loc.z,
            iconStorage.getIcon("blood"), false
        )

        object : BukkitRunnable() {
            override fun run() = marker.deleteMarker()
        }.runTaskLater(DynmapSimpleClans.getInstance(), config.section.getInt("visible-seconds", 60) * 20L)
    }

    private fun formatLabel(kill: Kill): String {
        val label: String = config.section.getString("format", "{vtag}&f{victim}|&7(killed by: {atag}&7{attacker}&7)")!!
            .replace("{victim}", kill.victim.name)
            .replace("{attacker}", kill.killer.name)
            .replace("{vtag}", kill.victim.clan?.tag ?: "")
            .replace("{atag}", kill.killer.clan?.tag ?: "")

        return Helper.colorToHTML(label)
    }
}