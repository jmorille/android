package eu.ttbox.osm.test.ui.map.mylocation;

import java.io.IOException;

import eu.ttbox.osm.tiles.svg.parser.SVGParseException;
import eu.ttbox.osm.ui.map.mylocation.CompassEnum;
import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;

public class CompassEnumTest extends AndroidTestCase {

    @SmallTest
    public void testCompassNord() throws SVGParseException, IOException {
        float[] nords = new float[] { 0f, 360f, -1f, 361f, -0.5f };
        for (float nord : nords) {
            CompassEnum compass = CompassEnum.getCardinalPoint(nord);
            assertNotNull(compass);
            assertEquals(CompassEnum.N, compass);
        }
    }

    @SmallTest
    public void testNegativeDegres() throws SVGParseException, IOException {
        CompassEnum compass = CompassEnum.getCardinalPoint(-63f);
        assertNotNull(compass);
        assertEquals(CompassEnum.NE, compass);
    }
}
