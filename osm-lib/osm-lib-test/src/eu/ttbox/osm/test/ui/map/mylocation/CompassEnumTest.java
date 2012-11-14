package eu.ttbox.osm.test.ui.map.mylocation;

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
    
    @SmallTest
    public void testModulo() throws SVGParseException, IOException {
    	float degres = -63%360;
    	Log.w(TAG, "Modulo => " + degres);
    }
}
