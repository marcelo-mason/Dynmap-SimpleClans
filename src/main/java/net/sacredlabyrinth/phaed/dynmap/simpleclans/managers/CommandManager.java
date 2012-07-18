package net.sacredlabyrinth.phaed.dynmap.simpleclans.managers;

import net.sacredlabyrinth.phaed.dynmap.simpleclans.DynmapSimpleClans;
import net.sacredlabyrinth.phaed.dynmap.simpleclans.entries.PlayerEntry;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandManager implements CommandExecutor
{

    DynmapSimpleClans plugin;

    public CommandManager()
    {
        plugin = DynmapSimpleClans.getInstance();
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        try {
            if (command.getName().equals("map")) {
                Player player;

                if (sender instanceof Player) {
                    player = (Player) sender;

                    if (args.length > 0) {
                        String cmd = args[0];

                        if (cmd.equals("toggle") && player.hasPermission("simpleclans.map.toggle")) {
                            PlayerEntry entry = plugin.getPlayerManager().getEntry(player);

                            if (entry.isVisible()) {
                                entry.setVisible(false);
                                player.sendMessage(ChatColor.AQUA + "You are no longer visible on the map");
                            } else {
                                entry.setVisible(true);
                                player.sendMessage(ChatColor.AQUA + "You are now visible on the map");
                            }
                        }
                    }
                }
            }

            return true;
        } catch (Exception ex) {
            SimpleClans.debug("Command failure", ex);
        }

        return false;
    }
}
