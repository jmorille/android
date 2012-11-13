package eu.ttbox.geoping.test.osm;

import java.io.IOException;

import eu.ttbox.osm.tiles.svg.parser.SVGParseException;
import eu.ttbox.osm.ui.map.mylocation.CompassEnum;
import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;

public class CompassEnumTest extends AndroidTestCase {

    private static final String TAG = "CompassEnumTest";

    @SmallTest
    public void testCompassNord() throws SVGParseException, IOException {
        float[] nords = new float[] { 0f, 360f,359f, -1f, 361f, -0.5f , -10f};
        for (float nord : nords) {
            CompassEnum compass = CompassEnum.getCardinalPoint(nord);
            Log.d(TAG,String.format( "Compass %s¡  => %s",nord, compass));
            assertNotNull(compass);
            assertEquals(CompassEnum.N, compass);
        }
    }

    @SmallTest
    public void testNegativeDegres() throws SVGParseException, IOException {
        CompassEnum compass = CompassEnum.getCardinalPoint(-63f);
        assertNotNull(compass);
        assertEquals(CompassEnum.WNW, compass);
    }
}
