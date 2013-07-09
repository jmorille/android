package eu.ttbox.velib.service.download.velobleu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;

import org.json.JSONObject;

import eu.ttbox.velib.model.Station;
import eu.ttbox.velib.model.VelibProvider;
import eu.ttbox.velib.service.download.VeloServiceParser;

public class VeloBleuServiceParser implements VeloServiceParser {

	@Override
	public ArrayList<Station> parseInputStreamForStations(InputStream in, VelibProvider provider) {
		try {
			String json = convertInputStreamAsString(in, "UTF-8");
			JSONObject jObject = new JSONObject(json);
			JSONObject site = jObject.getJSONObject("site");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Station parseInputStreamForStationDispo(InputStream content, Station station) {
		// TODO Auto-generated method stub
		return null;
	}

	private String convertInputStreamAsString(InputStream in, String encoding) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(in, encoding));
		StringWriter sw = new StringWriter(1024);
		char[] bufText = new char[1024];
		int charRead = 0;
		while ((charRead = reader.read(bufText)) != -1) {
			sw.write(bufText, 0, charRead);
		}
		return sw.toString();
	}

}
