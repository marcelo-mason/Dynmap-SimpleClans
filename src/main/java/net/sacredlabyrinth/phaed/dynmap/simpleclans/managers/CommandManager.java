package net.sacredlabyrinth.phaed.dynmap.simpleclans.managers;

import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.sacredlabyrinth.phaed.dynmap.simpleclans.DynmapSimpleClans;
import net.sacredlabyrinth.phaed.dynmap.simpleclans.entries.PlayerEntry;
import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;
import org.jetbrains.annotations.NotNull;

public class CommandManager implements CommandExecutor {

	DynmapSimpleClans plugin;

	public CommandManager() {
		plugin = DynmapSimpleClans.getInstance();
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, Command command, @NotNull String label, String[] args) {
		if (command.getName().equals("clanmap")) {
			if (args.length > 0) {
				String cmd = args[0];
				if (cmd.equalsIgnoreCase("help")) {
					processHelpCommand(sender);
					return true;
				}
				
				if (cmd.equalsIgnoreCase("reload")) {
					return processReloadCommand(sender);
				}

				if (sender instanceof Player) {
					Player player = (Player) sender;
					if (cmd.equalsIgnoreCase("seticon") && args.length == 2) {
						return processSetIconCommand(player, args[1]);
					}
					if (cmd.equalsIgnoreCase("listicons")) {
						return processListIconsCommand(player);
					}
					if (cmd.equalsIgnoreCase("toggle")) {
						return processToggleCommand(player);
					}
				}
			} else {
				processHelpCommand(sender);
				return true;
			}
		}
		return false;
	}

	private void processHelpCommand(@NotNull CommandSender sender) {
		plugin.getConfig().getStringList("help-command").forEach(s -> sender.sendMessage(ChatColor.translateAlternateColorCodes('&', s)));
	}

	private boolean processToggleCommand(Player player) {
		if (player.hasPermission("simpleclans.map.toggle")) {
			PlayerEntry entry = plugin.getPlayerManager().getEntry(player);

			if (entry.isVisible()) {
				entry.setVisible(false);
				player.sendMessage(plugin.getLang("not-visible"));
			} else {
				entry.setVisible(true);
				player.sendMessage(plugin.getLang("visible"));
			}
		} else {
			player.sendMessage(plugin.getLang("no-permission"));
		}

		return true;
	}

	private boolean processListIconsCommand(Player player) {
		if (!player.hasPermission("simpleclans.map.list")) {
			player.sendMessage(plugin.getLang("no-permission"));
			return true;
		}

		player.sendMessage(plugin.getLang("available-icons"));
		Set<String> icons = plugin.getClanHomes().getIcons();
		icons.forEach(icon -> player.sendMessage(plugin.getLang("icon-line").replace("@icon", icon)));
		if (icons.isEmpty()) {
			player.sendMessage(plugin.getLang("error-no-icons"));
		}

		return true;
	}

	private boolean processReloadCommand(@NotNull CommandSender sender) {
		if (!sender.hasPermission("simpleclans.map.reload")) {
			sender.sendMessage(plugin.getLang("no-permission"));
			return true;
		}

		sender.sendMessage(plugin.getLang("reloading"));
		plugin.cleanup();
		PreferencesManager.loadPreferences();
		plugin.reloadConfig();
		plugin.activate();

		return true;
	}

	private boolean processSetIconCommand(@NotNull Player player, @NotNull String icon) {
		ClanPlayer cp = plugin.getClanManager().getClanPlayer(player);
		if (cp == null || cp.getClan() == null) {
			player.sendMessage(plugin.getLang("not-member"));
			return true;
		}
		if (!player.hasPermission("simpleclans.map.seticon") || !cp.isLeader()) {
			player.sendMessage(plugin.getLang("no-permission"));
			return true;
		}

		if (plugin.getClanHomes().getIcons().contains(icon.toLowerCase())) {
			if (!player.hasPermission("simpleclans.map.icon.bypass") &&
					!player.hasPermission("simpleclans.map.icon." + icon)) {
				player.sendMessage(plugin.getLang("no-permission"));
				return true;
			}
			PreferencesManager pm = new PreferencesManager(cp.getClan());
			pm.setClanHomeIcon(icon);
			player.sendMessage(plugin.getLang("icon-changed"));
		} else {
			player.sendMessage(plugin.getLang("icon-not-found"));
		}

		return true;
	}
}