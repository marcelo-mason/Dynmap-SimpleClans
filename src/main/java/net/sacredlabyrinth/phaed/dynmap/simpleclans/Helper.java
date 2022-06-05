package net.sacredlabyrinth.phaed.dynmap.simpleclans;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Helper {

    // Suppresses default constructor, ensuring non-instantiability.
    private Helper() {
    }

    /**
     * The expression universally identifies a color code.
     *
     * <pre>
     * Format: "&<color_code><text>"
     * Valid examples: "&fHello", "Wo&1rld"
     * </pre>
     */
    private static final Pattern COLOR_CODE = Pattern.compile("&(?<color>[\\da-f])(?<text>[^&]+)");
    private static final String HTML_COLOR = "<span style='color: %s;'>%s</span>";

    /**
     * Converts a string with color codes to {@literal <span>} with inline css, pipes to {@literal <br>}
     *
     * @return colored html {@literal <span>}
     */
    public static String colorToHTML(@NotNull String string) {
        string = string.trim().replace("|", "<br>");
        Matcher matcher = COLOR_CODE.matcher(string);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String color = matcher.group("color");
            String text = matcher.group("text");
            ChatColor chatColor = ChatColor.getByChar(color);
            if (chatColor == null) {
                continue;
            }

            String htmlEncoded = String.format(HTML_COLOR, HEX.of(chatColor).getCode(), text);
            matcher.appendReplacement(sb, htmlEncoded);
        }

        matcher.appendTail(sb);
        return sb.toString();
    }

    public enum HEX {
        WHITE("#FFFFFF"),
        BLACK("#000000"),
        DARK_GRAY("#555555"),
        GRAY("#AAAAAA"),
        DARK_PURPLE("#AA00AA"),
        LIGHT_PURPLE("#FF55FF"),
        DARK_BLUE("#0000AA"),
        BLUE("#5555FF"),
        DARK_AQUA("#00AAAA"),
        AQUA("#55FFFF"),
        GREEN("#55FF55"),
        DARK_GREEN("#00AA00"),
        YELLOW("#FFFF55"),
        GOLD("#FFAA00"),
        RED("#FF5555"),
        DARK_RED("#AA0000");

        public String getCode() {
            return code;
        }

        private final String code;

        HEX(String code) {
            this.code = code;
        }

        public static HEX of(@NotNull ChatColor chatColor) {
            switch (chatColor.getChar()) {
                case '0':
                    return HEX.BLACK;
                case '1':
                    return HEX.DARK_BLUE;
                case '2':
                    return HEX.DARK_GREEN;
                case '3':
                    return HEX.DARK_AQUA;
                case '4':
                    return HEX.DARK_RED;
                case '5':
                    return HEX.DARK_PURPLE;
                case '6':
                    return HEX.GOLD;
                case '7':
                    return HEX.GRAY;
                case '8':
                    return HEX.DARK_GRAY;
                case '9':
                    return HEX.BLUE;
                case 'a':
                    return HEX.GREEN;
                case 'b':
                    return HEX.AQUA;
                case 'c':
                    return HEX.RED;
                case 'd':
                    return HEX.LIGHT_PURPLE;
                case 'e':
                    return HEX.YELLOW;
                default:
                    return HEX.WHITE;
            }
        }
    }
}
