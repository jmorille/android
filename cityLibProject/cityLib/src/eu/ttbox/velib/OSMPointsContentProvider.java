package eu.ttbox.velib;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;

/**
 * @see @{link http://androidgps.blogspot.fr/2008/10/openstreetmap-contentprovider-for.html} 
 *
 */
public class OSMPointsContentProvider extends ContentProvider {

	private final String TAG = "OSMPointsContentProvider";
 

	private final String osmxapiserver = "http://www.informationfreeway.org/api/0.5/";


	public static class Constants {
		public static final String AUTHORITY = "org.osm.data";
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/osmdata");
		
		public static final String COLLECTION_MIME_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.osmdata";
		public static final String ITEM_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.osmdata";
	}

	@Override
	public boolean onCreate() { 
		return true;
	}
 

	@Override
	public String getType(Uri uri) { 
		return Constants.COLLECTION_MIME_TYPE;
	}
 

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		String[] columnNames = { "lat", "lon" };
		OSMPointCursor result = new OSMPointCursor(columnNames);
		URL url = null;
		if (null == selection) {
			selection = "*=*";
		}
		try {
			url = new URL(constructQueryUrl(selection, selectionArgs[0], selectionArgs[1], selectionArgs[2], selectionArgs[3]));
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			XMLReader xmlReader = sp.getXMLReader();
			OSMHandler handler = new OSMHandler(result);
			xmlReader.setContentHandler(handler);
			xmlReader.setErrorHandler(handler);
			xmlReader.parse(new InputSource(url.openStream()));
		} catch (MalformedURLException e) {
			Log.e(TAG, e.getMessage());
		} catch (IOException e) {
			Log.e(TAG, "IOException " + e.getMessage());
		} catch (SAXException e) {
			Log.e(TAG, "SAX " + e.getMessage());
		} catch (ParserConfigurationException e) {
			Log.e(TAG, "PC Exception " + e.getMessage());
		}

		return result;
	}
	
	protected String constructQueryUrl(String selection, Double left, Double bottom, Double right, Double top) {
		String query = String.format("node[%][bbox=%f,%f,%f,%f]", selection, left, bottom, right, top);
		String url = osmxapiserver + query;
		return url;
	}

	protected String constructQueryUrl(String selection, String top, String left, String bottom, String right) {
		String query = String.format("node[%s][bbox=%s,%s,%s,%s]", selection, left, bottom, right, top);
		String url = osmxapiserver + query;
		return url;
	}


	

	@Override
	public Uri insert(Uri uri, ContentValues values) { 
		return null;
	}
	
	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) { 
		return 0;
	}
	
	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) { 
		return 0;
	}

	private class OSMPointCursor extends MatrixCursor {
		public OSMPointCursor(String[] columnNames) {
			super(columnNames);
		}
	}


	
	private class OSMHandler extends DefaultHandler {
		private OSMPointCursor cursor;

		OSMHandler(OSMPointCursor cursor) {
			super();
			this.cursor = cursor;
		}

		public void startElement(String uri, String name, String qName, Attributes atts) {
			if (name.equals("node")) {
				Object[] columnValues = { atts.getValue("lat"), atts.getValue("lon") };
				cursor.addRow(columnValues);
			}
		}
	}
	
}
