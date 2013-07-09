package eu.ttbox.velib.service.download;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import eu.ttbox.velib.model.Station;
import eu.ttbox.velib.model.VelibProvider;

public interface VeloServiceParser {

	ArrayList<Station> parseInputStreamForStations(InputStream in, VelibProvider provider) throws IOException;

	Station parseInputStreamForStationDispo(InputStream content, Station station);

}