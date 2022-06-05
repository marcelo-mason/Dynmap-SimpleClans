package net.sacredlabyrinth.phaed.dynmap.simpleclans.tasks

import net.sacredlabyrinth.phaed.dynmap.simpleclans.DynmapSimpleClans
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable

class HideWarringClansTask(val plugin: DynmapSimpleClans) : BukkitRunnable() {

    init {
        this.runTaskTimerAsynchronously(plugin, 0, TASK_INTERVAL)
    }

    override fun run() {
        for (player in Bukkit.getOnlinePlayers()) {
            val clanPlayer = plugin.clanManager.getClanPlayer(player) ?: continue

            plugin.dynmapApi.assertPlayerInvisibility(player, clanPlayer.clan!!.warringClans.isNotEmpty(), plugin)
        }
    }

    companion object {
        const val TASK_INTERVAL: Long = 60 * 20L
    }
}