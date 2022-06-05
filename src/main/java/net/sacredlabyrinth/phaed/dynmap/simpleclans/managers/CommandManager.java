package net.sacredlabyrinth.phaed.dynmap.simpleclans.managers;

import net.sacredlabyrinth.phaed.dynmap.simpleclans.DynmapSimpleClans;
import net.sacredlabyrinth.phaed.dynmap.simpleclans.Preferences;
import net.sacredlabyrinth.phaed.dynmap.simpleclans.layers.HomeLayer;
import net.sacredlabyrinth.phaed.simpleclans.Clan;
import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.dynmap.markers.MarkerIcon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static net.sacredlabyrinth.phaed.dynmap.simpleclans.DynmapSimpleClans.lang;

public final class CommandManager implements TabExecutor {

    private final @NotNull DynmapSimpleClans plugin;
    private final @Nullable HomeLayer homeLayer;

    public CommandManager(@NotNull DynmapSimpleClans plugin) {
        this.plugin = plugin;
        homeLayer = plugin.getHomeLayer();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command command, @NotNull String label, String[] args) {
        if (command.getName().equals("clanmap")) {
            if (args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("help"))) {
                help(sender);
                return true;
            }

            String cmd = args[0];
            if (cmd.equalsIgnoreCase("reload")) {
                return reload(sender);
            }

            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (cmd.equalsIgnoreCase("seticon") && args.length == 2) {
                    return setIcon(player, args[1]);
                }
            }
        }
        return false;
    }

    /**
     * Shows help command to the sender
     */
    private void help(@NotNull CommandSender sender) {
        plugin.getConfig().getStringList("help-command").forEach(s ->
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', s)));
    }

    /**
     * Reloads the plugin
     */
    private boolean reload(@NotNull CommandSender sender) {
        if (!sender.hasPermission("simpleclans.map.reload")) {
            sender.sendMessage(lang("no-permission"));
            return true;
        }

        sender.sendMessage(lang("reloading"));
        plugin.reload();

        return true;
    }

    private boolean setIcon(@NotNull Player player, @NotNull String icon) {
        ClanPlayer cp = plugin.getClanManager().getClanPlayer(player);
        if (cp == null) {
            player.sendMessage(lang("not-member"));
            return true;
        }

        // Can't be null because of the check in clanManager#getClanPlayer
        Clan clan = Objects.requireNonNull(cp.getClan());

        if (!player.hasPermission("simpleclans.map.seticon") || !cp.isLeader()) {
            player.sendMessage(lang("no-permission"));
            return true;
        }

        if (homeLayer == null) {
            player.sendMessage("layer-disabled");
            return true;
        }

        if (homeLayer.getIconStorage().has(icon.toLowerCase())) {
            if (!player.hasPermission("simpleclans.map.icon.bypass") &&
                    !player.hasPermission("simpleclans.map.icon." + icon)) {
                player.sendMessage(lang("no-permission"));
                return true;
            }

            Preferences pm = new Preferences(clan);
            pm.setClanHomeIcon(icon);
            homeLayer.upsertMarker(clan);
            player.sendMessage(lang("icon-changed"));
        } else {
            player.sendMessage(lang("icon-not-found"));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!command.getName().equalsIgnoreCase("clanmap")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            return Arrays.asList("seticon", "help");
        }

        if (args[0].equalsIgnoreCase("seticon")) {
            if (homeLayer == null) {
                return Collections.emptyList();
            }

            if (!sender.hasPermission("simpleclans.map.list")) {
                sender.sendMessage(lang("no-permission"));
                return Collections.emptyList();
            }

            List<String> icons = homeLayer.getIconStorage().getIcons().stream().
                    map(MarkerIcon::getMarkerIconLabel).
                    collect(Collectors.toList());

            if (icons.isEmpty()) {
                sender.sendMessage(lang("error-no-icons"));
                return Collections.emptyList();
            }

            return icons;
        }

        return Collections.emptyList();
    }
}