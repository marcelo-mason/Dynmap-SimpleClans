package net.sacredlabyrinth.phaed.dynmap.simpleclans.layers

import net.sacredlabyrinth.phaed.dynmap.simpleclans.layers.LayerConfig.LayerField.*
import org.dynmap.markers.MarkerAPI
import org.dynmap.markers.MarkerSet

abstract class Layer(
    private val id: String,
    protected val config: LayerConfig,
    markerAPI: MarkerAPI
) {

    val markerSet: MarkerSet

    init {
        check(config.getBoolean(ENABLE)) { "Layer $id is disabled!" }

        markerSet = markerAPI.getMarkerSet(id) ?: markerAPI.createMarkerSet(id, config.getString(LABEL), null, true)

        markerSet.markerSetLabel = config.getString(LABEL)
        markerSet.layerPriority = config.getInt(PRIORITY)
        markerSet.hideByDefault = config.getBoolean(HIDDEN)
        markerSet.minZoom = config.getInt(MINZOOM)
    }
}