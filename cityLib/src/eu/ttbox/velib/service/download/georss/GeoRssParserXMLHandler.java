package eu.ttbox.velib.service.download.georss;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

/**
 * Sample: http://earthquake.usgs.gov/earthquakes/catalogs/1day-M2.5.xml Sample: http://digital.cs.usu.edu/~vkulyukin/vkweb/earthquakes.xml
 *  
 */
public class GeoRssParserXMLHandler extends DefaultHandler {

	private String TAG = getClass().getSimpleName();

	private static final String ELT_ENTRY = "entry";
	private static final String ELT_TITLE = "title";
	private static final String ELT_LINK = "link";
	private static final String ELT_SUMMARY = "summary";
	private static final String ELT_GEOPOINT_LAT_LNG = "georss:point"; // <georss:point>-3.3042 100.4052</georss:point>
	private static final String ELT_GEOPOINT_ELV = "georss:elev";
	private static final String ELT_CATEGORY = "category";

	private boolean inEntry = false;

	private GeoRssEntry geoEntry;

	private ArrayList<GeoRssEntry> geoEntries;

	public ArrayList<GeoRssEntry> getGeoEntries() {
		return geoEntries;
	}

	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
		geoEntries = new ArrayList<GeoRssEntry>();
	}

	/*
	 * Fonction étant déclenchée lorsque le parser trouve un tag XML C'est cette méthode que nous allons utiliser pour instancier un nouveau feed
	 */
	@Override
	public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {

		if (localName.equalsIgnoreCase(ELT_ENTRY)) {
			if (Log.isLoggable(TAG, Log.DEBUG)) {
				Log.d(TAG, String.format("Start Entry : %s", localName));
			}
			inEntry = true;
			geoEntry = new GeoRssEntry();

		}
	}

	@Override
	public void endElement(String uri, String localName, String name) throws SAXException {
		if (localName.equalsIgnoreCase(ELT_ENTRY)) {
			if (Log.isLoggable(TAG, Log.DEBUG)) {
				Log.d(TAG, String.format("End   Entry : %s", localName));
			}
			geoEntries.add(geoEntry);
			geoEntry = null;
			inEntry = false;
		}

	}

	@Override
	public void processingInstruction(String target, String data) throws SAXException {
		if (Log.isLoggable(TAG, Log.DEBUG)) {
			Log.d(TAG, String.format("processingInstruction target : %s // Data :  %s", target, data));
		}
		if (target.equalsIgnoreCase(ELT_GEOPOINT_LAT_LNG)) {
			int dataLenght = data.length();
			int sepIdx = data.indexOf(' ');
			if (sepIdx > -1) {
				double lat = Double.valueOf(data.substring(0, sepIdx));
				double lng = Double.valueOf(data.substring(sepIdx + 1, dataLenght));
				geoEntry.setLatitude(lat);
				geoEntry.setLongitude(lng);
			}
		}
		super.processingInstruction(target, data);
	}

}
