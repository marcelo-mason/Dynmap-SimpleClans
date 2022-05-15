package net.sacredlabyrinth.phaed.dynmap.simpleclans;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HelperTest {

    @Test
    @DisplayName("Tests if provided string can be translated to html hex")
    void colorToHexTest() {
        String expected = "<span style='color: #FFFFFF;'>Hello<br></span><span style='color: #FF55FF;'>World</span>";
        String actual = Helper.colorToHTML("&fHello|&dWorld");

        assertEquals(expected, actual);
    }
}
