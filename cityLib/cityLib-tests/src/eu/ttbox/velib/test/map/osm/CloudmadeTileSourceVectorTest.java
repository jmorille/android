package eu.ttbox.velib.test.map.osm;

import java.io.InputStream;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;
import eu.ttbox.osm.tiles.svg.parser.SVG;
import eu.ttbox.osm.tiles.svg.parser.SVGParser;

//import com.larvalabs.svgandroid.SVG;
//import com.larvalabs.svgandroid.SVGParseException;
//import com.larvalabs.svgandroid.SVGParser;

public class CloudmadeTileSourceVectorTest extends AndroidTestCase {

	private final static String TAG = "CloudmadeTileSourceVectorTest";

	// private CloudmadeTileSourceVector getService() {
	// CloudmadeTileSourceVector service = new CloudmadeTileSourceVector( //
	// "CloudMadeVectorTiles", ResourceProxy.string.cloudmade_small, 0, 21, 256,
	// ".svg", // svgz
	// "http://alpha.vectors.cloudmade.com/%s/%d/%d/%d/%d/%d%s?token=%s" //
	// );
	// return service;
	// }

//	@SmallTest
//	public void testSvg2DrawableShapesFromAssets() throws SVGParseException, IOException {
//		AssetManager assets = getContext().getAssets();
//		SVG svg = SVGParser.getSVGFromAsset(assets, "svg/shapes.svg");
//		Log.d(TAG, "Open To SVG file : " + svg);
//		Drawable img = svg.createPictureDrawable();
//		Log.d(TAG, "Open Drawable img : " + img);
//	}
//	@SmallTest
//	public void testSvg2DrawableShapesFromAssetsIs() throws SVGParseException, IOException {
//		AssetManager assets = getContext().getAssets();
//		InputStream is =  assets.open("svg/shapes.svg");
//		SVG svg = SVGParser.getSVGFromInputStream(is);
//		Log.d(TAG, "Open To SVG file : " + svg);
//		Drawable img = svg.createPictureDrawable();
//		Log.d(TAG, "Open Drawable img : " + img);
//	}
//
//	@SmallTest
//	public void testSvg2Drawableandroid() {
//		Context testContext = getContext();
//		InputStream is = testContext.getResources().openRawResource(eu.ttbox.velib.test.R.raw.shapes); // getClass().getResourceAsStream(resName);
//		SVG svg = SVGParser.getSVGFromInputStream(is);
//		Drawable img = svg.createPictureDrawable();
//		Log.d(TAG, "Open Drawable img : " + img);
//	}

	@SmallTest
	public void testSvg2Drawable() {
		Context testContext = getContext();
		InputStream is = testContext.getResources().openRawResource(eu.ttbox.velib.test.R.raw.tiles_svg_45100); // getClass().getResourceAsStream(resName);
	
		SVG svg = SVGParser.getSVGFromInputStream(is);
		
		Drawable img = svg.createPictureDrawable();
		Log.d(TAG, "Open Drawable img : " + img);
	}

	// @SmallTest
	// public void testGetdrawable() {
	// CloudmadeTileSourceVector service = getService();
	// Log.d(TAG, "Open InputStream");
	// Context testContext = getContext();
	// InputStream is =
	// testContext.getResources().openRawResource(eu.ttbox.velib.test.R.raw.tiles_svg_45100);
	// // getClass().getResourceAsStream(resName);
	// Log.d(TAG, "Open InputStream true");
	// Drawable img = service.getDrawable(is);
	// Log.d(TAG, "Open Drawable img : " + img);
	// }

}
