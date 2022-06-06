package net.sacredlabyrinth.phaed.dynmap.simpleclans.tasks

import net.sacredlabyrinth.phaed.dynmap.simpleclans.DynmapSimpleClans
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable

class HideWarringClansTask(val plugin: DynmapSimpleClans) : BukkitRunnable() {
    private val taskInterval: Long = 60 * 20L
    init {
        this.runTaskTimerAsynchronously(plugin, 0, taskInterval)
    }

    override fun run() {
        for (player in Bukkit.getOnlinePlayers()) {
            val clanPlayer = plugin.clanManager.getClanPlayer(player) ?: continue
            // Clan can't be null because of the check in ClanPlayer#getClanPlayer
            plugin.dynmapApi.assertPlayerInvisibility(player, clanPlayer.clan!!.warringClans.isNotEmpty(), plugin)
        }
    }
}