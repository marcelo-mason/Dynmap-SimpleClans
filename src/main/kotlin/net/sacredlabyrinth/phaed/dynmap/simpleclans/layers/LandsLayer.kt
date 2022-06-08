package net.sacredlabyrinth.phaed.dynmap.simpleclans.layers

import net.sacredlabyrinth.phaed.dynmap.simpleclans.DynmapSimpleClans.debug
import net.sacredlabyrinth.phaed.dynmap.simpleclans.Helper
import net.sacredlabyrinth.phaed.dynmap.simpleclans.Helper.HEXColor
import net.sacredlabyrinth.phaed.simpleclans.Clan
import net.sacredlabyrinth.phaed.simpleclans.hooks.protection.Coordinate
import net.sacredlabyrinth.phaed.simpleclans.hooks.protection.Land
import net.sacredlabyrinth.phaed.simpleclans.managers.ClanManager
import net.sacredlabyrinth.phaed.simpleclans.managers.ProtectionManager
import org.dynmap.markers.AreaMarker
import org.dynmap.markers.MarkerAPI
import java.util.*

class LandsLayer(
    clanManager: ClanManager,
    private val protectionManager: ProtectionManager,
    config: LayerConfig,
    markerAPI: MarkerAPI
) : Layer("simpleclans.layers.lands", config, markerAPI) {

    init {
        // Delete old markers
        markerSet.areaMarkers.forEach(AreaMarker::deleteMarker)

        // Create/update new markers
        clanManager.clans.forEach(this::upsertMarker)
    }


    fun upsertMarker(clan: Clan) {
        val tag = clan.tag
        val loc = clan.homeLocation ?: return
        val world = loc.world ?: return
        val worldName = world.name
        val label = Helper.getClanLabel(config, clan)

        val lands = protectionManager.getLandsAt(loc)
        val coordinates = lands.map(Land::getCoordinates).flatMap(MutableList<Coordinate>::toList)
        val xCoords = coordinates.map(Coordinate::getX).toDoubleArray()
        val zCoords = coordinates.map(Coordinate::getZ).toDoubleArray()

        if (xCoords.isEmpty() || zCoords.isEmpty()) {
            debug("No coordinates found for $tag clan.")
            debug("Coords are X:${xCoords.asList()}, Z:${xCoords.asList()}")
            return
        }

        if (isHidden(tag)) {
            debug("[LandsLayer] Marker can't be updated/created, because clan($tag) is hidden!")
            return
        }

        val marker = markerSet.findAreaMarker(tag)
            ?: markerSet.createAreaMarker(tag, label, true, worldName, xCoords, zCoords, true)

        var fillColor = config.getString("style.fill.color", "#57b356")
        var lineColor = config.getString("style.line.color", "#2d682d")
        if (config.getBoolean("style.based-on-tag", false)) {
            fillColor = HEXColor.of(clan.color).code
            lineColor = fillColor
        }

        val fillOpacity = config.getDouble("style.fill.opacity", 0.35)
        val lineOpacity = config.getDouble("style.line.opacity", 0.8)
        val lineWeight = config.getInt("style.line.weight", 3)

        marker.setLabel(label, true)
        marker.description = clan.description
        marker.setFillStyle(fillOpacity, Integer.valueOf(fillColor.replace("#", ""), 16))
        marker.setLineStyle(lineWeight, lineOpacity, Integer.valueOf(lineColor.replace("#", ""), 16))
        marker.setCornerLocations(xCoords, zCoords)
    }

    private fun isHidden(tag: String) = config.getStringList("hidden-lands").contains(tag)
}