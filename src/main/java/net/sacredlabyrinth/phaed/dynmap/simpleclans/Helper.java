package net.sacredlabyrinth.phaed.dynmap.simpleclans;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Helper {
    /**
     * Converts color codes to <span> with inline css, pipes to <br/>
     *
     * @param msg
     * @return
     */
    public static String colorToHTML(String msg)
    {
        String out = "";

        msg = msg.trim();
        msg = msg.replace("&", "\u00a7");
        msg = msg.replace("|", "<br/>");
        String[] sections = msg.split("\\u00a7");

        boolean doneFirst = false;
        boolean hasFirst = msg.charAt(0) == '\u00a7';

        for (String section : sections) {
            if (!section.isEmpty()) {
                if (!doneFirst && !hasFirst) {
                    out += section;
                    doneFirst = true;
                    continue;
                }

                if (section.length() == 1) {
                    continue;
                }

                String color = section.substring(0, 1);
                String text = section.substring(1);

                out += "<span style='color:" + colorCodeToHEX(color) + ";'>" + text + "</span>";
            }
        }

        return out;
    }

    private static String colorCodeToHEX(String code)
    {
        if (code.equalsIgnoreCase("0")) {
            return "#222";
        }
        if (code.equalsIgnoreCase("1")) {
            return "#00A";
        }
        if (code.equalsIgnoreCase("2")) {
            return "#0A0";
        }
        if (code.equalsIgnoreCase("3")) {
            return "#0AA";
        }
        if (code.equalsIgnoreCase("4")) {
            return "#A00";
        }
        if (code.equalsIgnoreCase("5")) {
            return "#A0A";
        }
        if (code.equalsIgnoreCase("6")) {
            return "#FA0";
        }
        if (code.equalsIgnoreCase("7")) {
            return "#AAA";
        }
        if (code.equalsIgnoreCase("8")) {
            return "#555";
        }
        if (code.equalsIgnoreCase("9")) {
            return "#55F";
        }
        if (code.equalsIgnoreCase("a")) {
            return "#5F5";
        }
        if (code.equalsIgnoreCase("b")) {
            return "#5FF";
        }
        if (code.equalsIgnoreCase("c")) {
            return "#F55";
        }
        if (code.equalsIgnoreCase("d")) {
            return "#F5F";
        }
        if (code.equalsIgnoreCase("e")) {
            return "#FF5";
        }
        if (code.equalsIgnoreCase("f")) {
            return "#FFF";
        }

        return "#FFF";
    }

    /**
     * Returns a prettier coordinate, does not include world
     *
     * @param loc
     * @return
     */
    public static String toLocationString(Location loc)
    {
        return loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ() + " " + loc.getWorld().getName();
    }
}
