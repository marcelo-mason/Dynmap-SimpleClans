package net.sacredlabyrinth.phaed.dynmap.simpleclans.managers;

import net.sacredlabyrinth.Phaed.PreciousStones.Helper;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import net.sacredlabyrinth.phaed.dynmap.simpleclans.ChatBlock;
import net.sacredlabyrinth.phaed.dynmap.simpleclans.DynmapSimpleClans;
import net.sacredlabyrinth.phaed.dynmap.simpleclans.entries.PlayerEntry;

import java.util.logging.Level;

public class CommandManager implements CommandExecutor
{
    DynmapSimpleClans plugin;

    public CommandManager()
    {
        plugin = DynmapSimpleClans.getInstance();
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        try
        {
            if (command.getName().equals("map"))
            {
                Player player = null;

                if (sender instanceof Player)
                {
                    player = (Player) sender;

                    if (args.length > 0)
                    {
                        String cmd = args[0];
                        args = Helper.removeFirst(args);

                        if (cmd.equals("toggle") && player.hasPermission("simpleclans.map.toggle"))
                        {
                            PlayerEntry entry = plugin.getPlayerManager().getEntry(player);

                            if (entry.isVisible())
                            {
                                entry.setVisible(false);
                                ChatBlock.sendMessage(player, ChatColor.AQUA + "You are no longer visible on the map");
                            }
                            else
                            {
                                entry.setVisible(true);
                                ChatBlock.sendMessage(player, ChatColor.AQUA + "You are now visible on the map");
                            }
                        }
                    }
                }
            }

            return true;
        }
        catch (Exception ex)
        {
            PreciousStones.log(Level.SEVERE, "Command failure: {0}", ex.getMessage());
        }

        return false;
    }
}
