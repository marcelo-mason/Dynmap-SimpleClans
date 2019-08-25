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

public class CommandManager implements CommandExecutor {

	DynmapSimpleClans plugin;

	public CommandManager() {
		plugin = DynmapSimpleClans.getInstance();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equals("clanmap")) {
			if (args.length > 0) {
				String cmd = args[0];
				if (cmd.equalsIgnoreCase("help")) {
					plugin.getConfig().getStringList("help-command").forEach(s -> sender.sendMessage(ChatColor.translateAlternateColorCodes('&', s)));
					return true;
				}
				
				if (cmd.equalsIgnoreCase("reload")) {
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

				if (sender instanceof Player) {
					Player player = (Player) sender;
					if (cmd.equalsIgnoreCase("seticon") && args.length == 2) {
						ClanPlayer cp = plugin.getClanManager().getClanPlayer(player);
						if (cp == null || cp.getClan() == null) {
							sender.sendMessage(plugin.getLang("not-member"));
							return true;
						}
						if (!sender.hasPermission("simpleclans.map.seticon") || !cp.isLeader()) {
							sender.sendMessage(plugin.getLang("no-permission"));
							return true;
						}
						
						String icon = args[1];
						if (plugin.getClanHomes().getIcons().contains(icon.toLowerCase())) {
							PreferencesManager pm = new PreferencesManager(cp.getClan());
							pm.setClanHomeIcon(icon);
							sender.sendMessage(plugin.getLang("icon-changed"));
						} else {
							sender.sendMessage(plugin.getLang("icon-not-found"));
						}
						
						return true;
					}
					if (cmd.equalsIgnoreCase("listicons")) {
						if (!player.hasPermission("simpleclans.map.list")) {
							sender.sendMessage(plugin.getLang("no-permission"));
							return true;
						}

						player.sendMessage(plugin.getLang("available-icons"));
						Set<String> icons = plugin.getClanHomes().getIcons();
						icons.forEach(icon -> {
							player.sendMessage(plugin.getLang("icon-line").replace("@icon", icon));
						});
						if (icons.isEmpty()) {
							player.sendMessage(plugin.getLang("error-no-icons"));
						}
						
						return true;
					}
					if (cmd.equalsIgnoreCase("toggle")) {
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
				}
			}
		}
		return false;
	}
}