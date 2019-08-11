package net.sacredlabyrinth.phaed.dynmap.simpleclans.managers;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.sacredlabyrinth.phaed.dynmap.simpleclans.DynmapSimpleClans;
import net.sacredlabyrinth.phaed.dynmap.simpleclans.entries.PlayerEntry;

public class CommandManager implements CommandExecutor {

	DynmapSimpleClans plugin;

	public CommandManager() {
		plugin = DynmapSimpleClans.getInstance();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		try {
			if (command.getName().equals("map")) {
				Player player;

				if (sender instanceof Player) {
					player = (Player) sender;

					if (args.length > 0) {
						String cmd = args[0];

						if (cmd.equals("toggle")) {
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
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return false;
	}
}
