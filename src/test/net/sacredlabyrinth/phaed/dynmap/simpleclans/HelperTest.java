package net.sacredlabyrinth.phaed.dynmap.simpleclans;

import net.sacredlabyrinth.phaed.dynmap.simpleclans.Helper.HEXColor;
import org.bukkit.ChatColor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HelperTest {

    public static Collection<Object[]> HtmlData() {
        return Arrays.asList(new Object[][]{
                {"Wo<span style='color: #0000AA;'>rld</span>", "Wo&1rld"},
                {"Clan <span style='color: #FFFFFF;'>Hello<br></span><span style='color: #FF55FF;'>World</span>", "Clan &fHello|&dWorld"},
        });
    }

    public static Collection<Object[]> hexValidityData() {
        return Arrays.asList(new Object[][]{
                {HEXColor.AQUA, HEXColor.of("b")},
                {HEXColor.RED, HEXColor.of(ChatColor.RED)},
                {HEXColor.WHITE, HEXColor.of(" ")},
                {HEXColor.DARK_GREEN, HEXColor.of("&2")},
                {HEXColor.LIGHT_PURPLE, HEXColor.of("§d")}
        });
    }

    @ParameterizedTest
    @DisplayName("Tests if provided string can be translated to encoded html")
    @MethodSource("HtmlData")
    void colorToHexTest(String expectedValue, String actualValue) {
        String actual = Helper.colorToHTML(actualValue);
        assertEquals(expectedValue, actual);
    }

    @ParameterizedTest
    @DisplayName("Tests the HEXColor#of validity")
    @MethodSource("hexValidityData")
    void hexColorValidityTest(HEXColor expectedValue, HEXColor actualValue) {
        assertEquals(expectedValue, actualValue);
    }
}
