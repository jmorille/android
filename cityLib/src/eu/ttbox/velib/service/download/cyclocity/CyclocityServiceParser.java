package eu.ttbox.velib.service.download.cyclocity;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import android.content.ContentValues;
import android.util.Log;
import eu.ttbox.velib.model.Arrondissement;
import eu.ttbox.velib.model.Station;
import eu.ttbox.velib.model.VelibProvider;
import eu.ttbox.velib.service.database.Velo.VeloColumns;
import eu.ttbox.velib.service.download.VeloServiceParser;
import eu.ttbox.velib.service.geo.GeoUtils;

public class CyclocityServiceParser implements VeloServiceParser {

	private String TAG = getClass().getSimpleName();

	private void readArrondissement(ArrayList<Arrondissement> arrondissements) {
		if (arrondissements != null && !arrondissements.isEmpty()) {
			int i = 0;
			for (Arrondissement arrondissement : arrondissements) {
				if (Log.isLoggable(TAG, Log.DEBUG))
					Log.d(TAG, String.format("%s : %s", ++i, arrondissement));
			}
		}
	}

	@Override
	public ArrayList<Station> parseInputStreamForStations(InputStream in, VelibProvider provider) throws IOException{
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			SAXParser parser = factory.newSAXParser();
			CyclocityCartoParserXMLHandler handler = new CyclocityCartoParserXMLHandler(provider);
			parser.parse(in, handler);
			// Manage Getting data
			ArrayList<Arrondissement> arrondissements = handler.getDataArrondissement();
			readArrondissement(arrondissements);
			// double[] boundyBoxE6 = arrondissement.getBoundyBoxE6();
			double[] boundyBoxE6 = handler.getStationsBoundyBoxE6();
			double[] previousBox = provider.getBoundyBoxE6();
			boolean isRedifineBoundy = GeoUtils.isRedefineBox(boundyBoxE6, previousBox);
			if (isRedifineBoundy) {
				// provider.setBoundyBoxE6(boundyBoxE6);
				Log.w(TAG, "BoundyBox for VelibProvider " + provider + " is Min " + String.format("(%f, %f)", boundyBoxE6[0], boundyBoxE6[1]) + " / Max "
						+ String.format("(%f, %f)", boundyBoxE6[2], boundyBoxE6[3]));
			}
			return handler.getData();
//		} catch (IOException e) {
// 			throw new RuntimeException("IOException : " + e.getMessage(), e);
		} catch (SAXException e) {
			throw new RuntimeException("SAXException : " + e.getMessage(), e);
		} catch (ParserConfigurationException e) {
			throw new RuntimeException("ParserConfigurationException : " + e.getMessage(), e);
		}
	}

	@Override
	public Station parseInputStreamForStationDispo(InputStream content, Station station) {
		DocumentBuilderFactory fabrique = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder docBuilder = fabrique.newDocumentBuilder();
			Document d = docBuilder.parse(content);
			// NodeList rootNode = d.getElementsByTagName("station");
			String availableString = d.getElementsByTagName("available").item(0).getTextContent();
			String totalString = d.getElementsByTagName("total").item(0).getTextContent();
			String freeString = d.getElementsByTagName("free").item(0).getTextContent();
			String ticketString = d.getElementsByTagName("ticket").item(0).getTextContent();
			// Result
			if (availableString != null && availableString.length() > 0) {
				station.setStationCycle(Integer.valueOf(availableString).intValue());
			}
			if (totalString != null && totalString.length() > 0) {
				station.setVeloTotal(Integer.valueOf(totalString).intValue());
			}
			if (freeString != null && freeString.length() > 0) {
				station.setStationParking(Integer.valueOf(freeString).intValue());
			}
			if (ticketString != null && ticketString.length() > 0) {
				station.setVeloTicket(Integer.valueOf(ticketString).intValue());
			} else {
				station.setVeloTicket(0);
			}
			// Manage Version Date
			station.setVeloUpdated(System.currentTimeMillis());
			return station;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}
	
	
	public ContentValues parseInputStreamForStationDispo(InputStream content ) {
        DocumentBuilderFactory fabrique = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder docBuilder = fabrique.newDocumentBuilder();
            Document d = docBuilder.parse(content);
            // NodeList rootNode = d.getElementsByTagName("station");
            String availableString = d.getElementsByTagName("available").item(0).getTextContent();
            String totalString = d.getElementsByTagName("total").item(0).getTextContent();
            String freeString = d.getElementsByTagName("free").item(0).getTextContent();
            String ticketString = d.getElementsByTagName("ticket").item(0).getTextContent();
            // Result
            ContentValues values = new ContentValues(6);
            if (availableString != null && availableString.length() > 0) {
                values.put(VeloColumns.COL_STATION_CYCLE, Integer.valueOf(availableString));
             }
            if (totalString != null && totalString.length() > 0) {
                values.put(VeloColumns.COL_STATION_TOTAL, Integer.valueOf(totalString)); 
            }
            if (freeString != null && freeString.length() > 0) {
                values.put(VeloColumns.COL_STATION_PARKING, Integer.valueOf(freeString));  
            }
            if (ticketString != null && ticketString.length() > 0) {
                values.put(VeloColumns.COL_STATION_TICKET, Integer.valueOf(ticketString));   
            } else {
                values.put(VeloColumns.COL_STATION_TICKET, Integer.valueOf(0));    
            }
            // Manage Version Date
            values.put(VeloColumns.COL_STATION_TICKET, Long.valueOf(System.currentTimeMillis()));  
            return values;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}
