package net.sacredlabyrinth.phaed.dynmap.simpleclans;

import net.sacredlabyrinth.phaed.dynmap.simpleclans.layers.LayerConfig;
import net.sacredlabyrinth.phaed.simpleclans.Clan;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.sacredlabyrinth.phaed.dynmap.simpleclans.DynmapSimpleClans.debug;
import static net.sacredlabyrinth.phaed.dynmap.simpleclans.DynmapSimpleClans.lang;

public final class Helper {

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

    // Suppresses default constructor, ensuring non-instantiability.
    private Helper() {
    }

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

            String htmlEncoded = String.format(HTML_COLOR, HEXColor.of(chatColor).getCode(), text);
            matcher.appendReplacement(sb, htmlEncoded);
        }

        matcher.appendTail(sb);
        return sb.toString();
    }

    public static String getClanLabel(LayerConfig config, Clan clan) {
        String inactive = String.format("%s/%s", clan.getInactiveDays(), clan.getMaxInactiveDays());
        String onlineMembers = String.format("%s/%s", clan.getOnlineMembers(), clan.getSize());
        String status = (clan.isVerified() ? lang("verified") : lang("unverified"));
        String feeEnabled = (clan.isMemberFeeEnabled() ? lang("fee-enabled") : lang("fee-disabled"));

        String label = config.getString("format", "{clan} &8(home)")
                .replace("{clan}", clan.getName())
                .replace("{tag}", clan.getTag())
                .replace("{member_count}", String.valueOf(clan.getMembers().size()))
                .replace("{inactive}", inactive)
                .replace("{founded}", clan.getFoundedString())
                .replace("{rival}", String.valueOf(clan.getTotalRival()))
                .replace("{neutral}", String.valueOf(clan.getTotalNeutral()))
                .replace("{deaths}", String.valueOf(clan.getTotalDeaths()))
                .replace("{kdr}", String.valueOf(clan.getTotalKDR()))
                .replace("{civilian}", String.valueOf(clan.getTotalCivilian()))
                .replace("{members_online}", onlineMembers)
                .replace("{leaders}", clan.getLeadersString("", ", "))
                .replace("{allies}", clan.getAllyString(", ", null))
                .replace("{rivals}", clan.getRivalString(", ", null))
                .replace("{fee_value}", String.valueOf(clan.getMemberFee()))
                .replace("{status}", status)
                .replace("{fee_enabled}", feeEnabled);

        return Helper.colorToHTML(label);
    }

    public enum HEXColor {
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

        private final String code;

        HEXColor(String code) {
            this.code = code;
        }

        public static HEXColor of(@NotNull ChatColor chatColor) {
            try {
                return HEXColor.valueOf(chatColor.name());
            } catch (IllegalArgumentException ex) {
                debug(String.format("Error while trying to parse the hex color of %s: %s", chatColor.name(), ex.getMessage()));
                return HEXColor.WHITE;
            }
        }

        public static HEXColor of(@NotNull String chatColorChar) {
            String color = chatColorChar.replace("§", "").replace("&", "");
            ChatColor chatColor = ChatColor.getByChar(color);
            return chatColor != null ? of(chatColor) : HEXColor.WHITE;
        }

        public String getCode() {
            return code;
        }
    }
}
