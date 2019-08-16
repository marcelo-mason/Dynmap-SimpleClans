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
				if (cmd.equalsIgnoreCase("reload")) {
					if (!sender.hasPermission("simpleclans.map.reload")) {
						sender.sendMessage(ChatColor.AQUA + "You don't have permission to do this!");
						return true;
					}

					sender.sendMessage(ChatColor.AQUA + "Reloading plugin...");
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
							sender.sendMessage(ChatColor.AQUA + "You must be member of a clan");
							return true;
						}
						if (!sender.hasPermission("simpleclans.map.seticon") || !cp.isLeader()) {
							sender.sendMessage(ChatColor.AQUA + "You don't have permission to do this!");
							return true;
						}
						
						String icon = args[1];
						if (plugin.getClanHomes().getIcons().contains(icon.toLowerCase())) {
							PreferencesManager pm = new PreferencesManager(cp.getClan());
							pm.setClanHomeIcon(icon);
							sender.sendMessage(ChatColor.AQUA + "The icon was set successfully! The change will take effect soon...");
						} else {
							sender.sendMessage(ChatColor.AQUA + "Icon not found!");
						}
						
						return true;
					}
					if (cmd.equalsIgnoreCase("listicons")) {
						if (!player.hasPermission("simpleclans.map.list")) {
							sender.sendMessage(ChatColor.AQUA + "You don't have permission to do this!");
							return true;
						}

						player.sendMessage(ChatColor.AQUA + "Available icons:");
						Set<String> icons = plugin.getClanHomes().getIcons();
						icons.forEach(icon -> {
							player.sendMessage(ChatColor.AQUA + "* " + icon);
						});
						if (icons.isEmpty()) {
							player.sendMessage(ChatColor.RED + "* Error, no icons available!");
						}
						
						return true;
					}
					if (cmd.equalsIgnoreCase("toggle")) {
						if (player.hasPermission("simpleclans.map.toggle")) {
							PlayerEntry entry = plugin.getPlayerManager().getEntry(player);

							if (entry.isVisible()) {
								entry.setVisible(false);
								player.sendMessage(ChatColor.AQUA + "You are no longer visible on the map");
							} else {
								entry.setVisible(true);
								player.sendMessage(ChatColor.AQUA + "You are now visible on the map");
							}
						} else {
							player.sendMessage(ChatColor.AQUA + "You don't have permission to do this!");
						}

						return true;
					}
				}
			}
		}
		return false;
	}
}