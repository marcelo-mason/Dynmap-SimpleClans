package net.sacredlabyrinth.phaed.dynmap.simpleclans.layers

import com.google.gson.JsonParser
import net.sacredlabyrinth.phaed.dynmap.simpleclans.DynmapSimpleClans.debug
import net.sacredlabyrinth.phaed.dynmap.simpleclans.Helper
import net.sacredlabyrinth.phaed.dynmap.simpleclans.IconStorage
import net.sacredlabyrinth.phaed.simpleclans.Clan
import net.sacredlabyrinth.phaed.simpleclans.managers.ClanManager
import org.dynmap.markers.Marker
import org.dynmap.markers.MarkerAPI

class HomesLayer(
    private val clanManager: ClanManager,
    val iconStorage: IconStorage,
    config: LayerConfig,
    markerAPI: MarkerAPI
) : Layer("simpleclans.layers.home", config, markerAPI) {

    init {
        // Delete old markers
        markerSet.markers.forEach(Marker::deleteMarker)

        // Create/update new markers
        getClansWithHome().forEach(this::upsertMarker)
    }

    fun upsertMarker(clan: Clan) {
        val tag = clan.tag
        val loc = clan.homeLocation
        val world = loc?.world ?: return
        val worldName = world.name

        val iconName = JsonParser().parse(clan.flags).asJsonObject.get("defaulticon")?.asString
            ?: iconStorage.defaultIconName
        val icon = iconStorage.getIcon(iconName)
        val label = Helper.getClanLabel(config, clan)

        if (isHidden(tag, worldName)) {
            debug("[HomesLayer] Marker can't be updated/created, because clan($tag) or world($worldName) is hidden!")
            return
        }

        val marker = markerSet.findMarker(tag) ?: markerSet.createMarker(
            tag, label, true, worldName, loc.x, loc.y, loc.z, icon, true
        )

        marker.setLocation(worldName, loc.x, loc.y, loc.z)
        marker.setLabel(label, true)
        marker.markerIcon = icon
    }

    private fun isHidden(tag: String, worldName: String): Boolean {
        val hidden = config.getStringList("hidden-markers")
        return hidden.contains(tag) || hidden.contains("world:$worldName")
    }

    private fun getClansWithHome(): Set<Clan> {
        return clanManager.clans
            .filter { clan -> clan.homeLocation != null }
            .filter { clan -> clan.homeLocation!!.world != null }
            .toSet()
    }
}