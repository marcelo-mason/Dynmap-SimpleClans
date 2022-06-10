package net.sacredlabyrinth.phaed.dynmap.simpleclans;

import org.dynmap.markers.MarkerAPI;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class IconStorageTest {

    @Mock
    MarkerAPI markerAPI;
    @Mock
    DynmapSimpleClans dynmapSimpleClans;

    @Test
    public void IsNotNull() {
        IconStorage iconStorage = new IconStorage(dynmapSimpleClans,
                "/images/clanhome", "clanhome", markerAPI);
        assertNotNull(iconStorage);
    }

    @Test
    @DisplayName("Tests the possibility of retrieving a stream from plugin's resources")
    public void defaultIconNotNull() {
        String path = "/images/clanhome/clanhome.png";
        try (InputStream is = DynmapSimpleClans.class.getResourceAsStream(path)) {
            assertNotNull(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
