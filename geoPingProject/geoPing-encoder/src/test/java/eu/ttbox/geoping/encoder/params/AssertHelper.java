package eu.ttbox.geoping.encoder.params;


import org.junit.Assert;

import java.util.Map;

import eu.ttbox.geoping.encoder.adapter.MapEncoderAdpater;

public class AssertHelper {


    public static boolean assertMap(MapEncoderAdpater expected, MapEncoderAdpater actual, boolean printIt) {
        return assertMap(expected.getMap(), actual.getMap(), printIt);
    }


    public static boolean assertMap(Map<String, Object> expected, Map<String, Object> actual, boolean printIt) {
        boolean isSame = true;
        Assert.assertEquals(expected.size(), actual.size());
        for (Map.Entry<String, Object> expect : expected.entrySet()) {
            Assert.assertTrue(actual.containsKey(expect.getKey()));
            Object actualVal = actual.get(expect.getKey());
            if (printIt) {
                System.out.println("assertMap for key [" + expect.getKey() + "] : " + expect.getValue() + " =? " + actualVal);
            }
            Assert.assertEquals(expect.getValue(), actualVal);
        }
        return isSame;
    }

}
