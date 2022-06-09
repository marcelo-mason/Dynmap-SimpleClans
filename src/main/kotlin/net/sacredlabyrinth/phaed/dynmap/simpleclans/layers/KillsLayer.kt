package net.sacredlabyrinth.phaed.dynmap.simpleclans.layers

import net.sacredlabyrinth.phaed.dynmap.simpleclans.DynmapSimpleClans
import net.sacredlabyrinth.phaed.dynmap.simpleclans.Helper
import net.sacredlabyrinth.phaed.dynmap.simpleclans.IconStorage
import net.sacredlabyrinth.phaed.dynmap.simpleclans.layers.LayerConfig.LayerField.FORMAT
import net.sacredlabyrinth.phaed.simpleclans.Kill
import org.bukkit.scheduler.BukkitRunnable
import org.dynmap.markers.MarkerAPI
import java.time.format.DateTimeFormatter

class KillsLayer(private val iconStorage: IconStorage, config: LayerConfig, markerAPI: MarkerAPI) :
    Layer("simpleclans.layers.kill", config, markerAPI) {

    private val timeFormat:DateTimeFormatter = DateTimeFormatter.ofPattern(config.getString("time-format", "HH:mm:ss"))

    fun createMarker(kill: Kill) {
        val vclan = kill.victim.clan
        val victim = kill.victim.toPlayer() ?: return
        val loc = victim.location
        val worldName = loc.world?.name

        if (vclan == null && !config.getBoolean("show.clan-players", true) ||
            vclan != null && !config.getBoolean("show.civilians", true)) {
            return
        }

        val marker = markerSet.createMarker(
            kill.time.toString(), formatLabel(kill), true,
            worldName, loc.x, loc.y, loc.z,
            iconStorage.getIcon(iconStorage.defaultIconName), false
        )

        object : BukkitRunnable() {
            override fun run() = marker.deleteMarker()
        }.runTaskLater(DynmapSimpleClans.getInstance(), config.getInt("visible-seconds", 300) * 20L)
    }

    private fun formatLabel(kill: Kill): String {
        val label: String = config.getString(FORMAT)
            .replace("{victim}", kill.victim.name)
            .replace("{attacker}", kill.killer.name)
            .replace("{vtag}", kill.victim.clan?.tag ?: "")
            .replace("{atag}", kill.killer.clan?.tag ?: "")
            .replace("{time}", kill.time.format(timeFormat))

        return Helper.colorToHTML(label)
    }
}