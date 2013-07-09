package eu.ttbox.velib.service.download.georss;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import eu.ttbox.velib.model.VelibProvider;

public class GeoRssServiceParser {

	public ArrayList<GeoRssEntry> parseInputStreamForStations(InputStream in, VelibProvider provider) {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			SAXParser parser = factory.newSAXParser();
			GeoRssParserXMLHandler handler = new GeoRssParserXMLHandler();
			parser.parse(in, handler);
			return handler.getGeoEntries();
		} catch (IOException e) {
			throw new RuntimeException("IOException : " + e.getMessage(), e);
		} catch (SAXException e) {
			throw new RuntimeException("SAXException : " + e.getMessage(), e);
		} catch (ParserConfigurationException e) {
			throw new RuntimeException("ParserConfigurationException : " + e.getMessage(), e);
		}
	}
}
