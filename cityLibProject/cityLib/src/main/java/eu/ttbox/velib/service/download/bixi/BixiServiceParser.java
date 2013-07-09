package eu.ttbox.velib.service.download.bixi;

import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import eu.ttbox.velib.model.Station;
import eu.ttbox.velib.model.VelibProvider;
import eu.ttbox.velib.service.download.VeloServiceParser;

public class BixiServiceParser implements VeloServiceParser {

	@Override
	public ArrayList<Station> parseInputStreamForStations(InputStream in, VelibProvider provider) {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			SAXParser parser = factory.newSAXParser();
			BixiParserXMLHandler handler = new BixiParserXMLHandler(provider.getProvider());
			parser.parse(in, handler);
			return handler.getData();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Station parseInputStreamForStationDispo(InputStream content, Station station) {
		// TODO Auto-generated method stub
		return null;
	}

}
