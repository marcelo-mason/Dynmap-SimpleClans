package net.sacredlabyrinth.phaed.dynmap.simpleclans;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HelperTest {

    @ParameterizedTest
    @DisplayName("Tests if provided string can be translated to encoded html")
    @MethodSource("data")
    void colorToHexTest(String expectedValue, String actualValue) {
        String actual = Helper.colorToHTML(actualValue);
        assertEquals(expectedValue, actual);
    }

    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"Wo<span style='color: #0000AA;'>rld</span>", "Wo&1rld"},
                {"Clan <span style='color: #FFFFFF;'>Hello<br></span><span style='color: #FF55FF;'>World</span>", "Clan &fHello|&dWorld"},
        });
    }
}
