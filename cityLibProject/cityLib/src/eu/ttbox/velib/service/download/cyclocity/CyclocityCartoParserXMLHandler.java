package eu.ttbox.velib.service.download.cyclocity;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import eu.ttbox.velib.model.Arrondissement;
import eu.ttbox.velib.model.Station;
import eu.ttbox.velib.model.VelibProvider;
import eu.ttbox.velib.service.geo.GeoUtils;

public class CyclocityCartoParserXMLHandler extends DefaultHandler {
	// nom des tags XML
	private static final String ITEM = "marker";
	private static final String ITEM_ARRONDISSEMENTS = "arrondissements";
	private static final String ITEM_ARRONDISSEMENT = "arrondissement ";
	private static final String ATR_ARRONDISSEMENT_NUMBER = "number ";
	private static final String ATR_ARRONDISSEMENT_MINLAT = "minLat ";
	private static final String ATR_ARRONDISSEMENT_MAXLAT = "maxLat ";
	private static final String ATR_ARRONDISSEMENT_MINLNG = "minLng ";
	private static final String ATR_ARRONDISSEMENT_MAXLNG = "maxLng ";
	private static final String ATTR_NAME = "name";
	private static final String ATTR_ADRESS = "address";
	private static final String ATTR_FULLADDRESS = "fullAddress";
	private static final String ATTR_NUMBER = "number";
	private static final String ATTR_LATITUDE = "lat";
	private static final String ATTR_LONGITUDE = "lng";
	private static final String ATTR_OPEN = "open";
	private static final String ATTR_BONUS = "bonus";

	// Array list de feeds
	private ArrayList<Station> entries;
	// Boolean permettant de savoir si nous sommes à l'intérieur d'un item
	private boolean inItem;
	private boolean inArrondissement;

	// Feed courant
	private Station currentFeed;

	private Arrondissement currentArrondissement;
	private ArrayList<Arrondissement> arrondissements;

	private VelibProvider provider;
	private int providerId;

	public CyclocityCartoParserXMLHandler(VelibProvider provider) {
		super();
		this.provider = provider;
		this.providerId = provider.getProvider();
	}

	@Override
	public void processingInstruction(String target, String data) throws SAXException {
		super.processingInstruction(target, data);
	}

	// * Cette méthode est appelée par le parser une et une seule
	// * fois au démarrage de l'analyse de votre flux xml.
	// * Elle est appelée avant toutes les autres méthodes de l'interface,
	// * à l'exception unique, évidemment, de la méthode setDocumentLocator.
	// * Cet événement devrait vous permettre d'initialiser tout ce qui doit
	// * l'être avant ledébut du parcours du document.
	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
		entries = new ArrayList<Station>();
		arrondissements = new ArrayList<Arrondissement>();
	}

	/*
	 * Fonction étant déclenchée lorsque le parser trouve un tag XML C'est cette méthode que nous allons utiliser pour instancier un nouveau feed
	 */
	@Override
	public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {

		// Ci dessous, localName contient le nom du tag rencontré
		// Nous avons rencontré un tag ITEM, il faut donc instancier un nouveau
		// feed
		if (name.equalsIgnoreCase(ITEM)) {
			this.currentFeed = new Station();
			currentFeed.setProvider(providerId);
			String stationNumber = attributes.getValue(ATTR_NUMBER);
			String stationName = parseStationName(attributes.getValue(ATTR_NAME), stationNumber);
			currentFeed.setNumber(stationNumber);
			currentFeed.setName(stationName);
			currentFeed.setAddress(parseStationAddress(attributes.getValue(ATTR_ADRESS)));
			currentFeed.setFullAddress(attributes.getValue(ATTR_FULLADDRESS));
			currentFeed.setLatitude(Double.parseDouble(attributes.getValue(ATTR_LATITUDE)));
			currentFeed.setLongitude(Double.parseDouble(attributes.getValue(ATTR_LONGITUDE)));
			currentFeed.setOpen(parseBoolean(attributes.getValue(ATTR_OPEN)));
			currentFeed.setBonus(parseBoolean(attributes.getValue(ATTR_BONUS)));
			inItem = true;
		} else if (name.equalsIgnoreCase(ITEM_ARRONDISSEMENT)) {
			this.currentArrondissement = new Arrondissement(System.currentTimeMillis());
			currentArrondissement.setNumber(attributes.getValue(ATR_ARRONDISSEMENT_NUMBER));
			currentArrondissement.setMinLatitude(Double.parseDouble(attributes.getValue(ATR_ARRONDISSEMENT_MINLAT)));
			currentArrondissement.setMinLongitude(Double.parseDouble(attributes.getValue(ATR_ARRONDISSEMENT_MINLNG)));
			currentArrondissement.setMaxLatitude(Double.parseDouble(attributes.getValue(ATR_ARRONDISSEMENT_MAXLAT)));
			currentArrondissement.setMaxLongitude(Double.parseDouble(attributes.getValue(ATR_ARRONDISSEMENT_MAXLNG)));
			inArrondissement = true;
		}
	}

	private String parseStationAddress(String stationAddress) {
		String result = stationAddress;
		if (result != null && result.length() > 0) {
			if (result.endsWith("-")) {
				result = result.substring(0, result.length()-1);
			}
			// Trim
			result = result.trim();
 		}
		return result;
	}
	private String parseStationName(String stationName, String stationNumber) {
		String result = stationName;
		if (result != null && result.length() > 0) {
			// Parse Number
			int idxSep = result.indexOf('-');
			if (idxSep > 0 && result.indexOf(stationNumber) < idxSep) {
				result = result.substring(idxSep + 1, result.length()).trim();
			}
			// Parse '( ' & ' )'
			if (result.indexOf('(') > 0) {
				result = result.replaceAll("\\( ", "\\(");
				result = result.replaceAll(" \\)", "\\)");
			}
			// Trim
			result = result.trim();
		}
		return result;
	}

	private boolean parseBoolean(String value) {
		if ("1".equals(value)) {
			return true;
		}
		return false;
	}

	// * Fonction étant déclenchée lorsque le parser à parsé
	// * l'intérieur de la balise XML La méthode characters
	// * a donc fait son ouvrage et tous les caractère inclus
	// * dans la balise en cours sont copiés dans le buffer
	// * On peut donc tranquillement les récupérer pour compléter
	// * notre objet currentFeed
	@Override
	public void endElement(String uri, String localName, String name) throws SAXException {
		if (name.equalsIgnoreCase(ITEM)) {
			entries.add(currentFeed);
			inItem = false;
		} else if (name.equalsIgnoreCase(ITEM_ARRONDISSEMENT)) {
			arrondissements.add(currentArrondissement);
			inArrondissement = false;
		}
	}

	// cette méthode nous permettra de récupérer les données
	public ArrayList<Station> getData() {
		return entries;
	}

	public double[] getStationsBoundyBox() {
		return getBoundyBox(entries);
	}

	public double[] getStationsBoundyBoxE6() {
		double[] boundyBoxE6 = GeoUtils.getBoundyBoxE6(entries, provider.getExcludeBoundyBoxE6());
		return boundyBoxE6;
	}

	private double[] getBoundyBox(ArrayList<Station> stations) {
		double[] boundyBox = GeoUtils.getBoundyBox(entries);
		return boundyBox;
	}

	public ArrayList<Arrondissement> getDataArrondissement() {
		if ((arrondissements == null || arrondissements.size() < 1) && entries != null && !entries.isEmpty()) {
			double[] boundyBox = getBoundyBox(entries);
			double minLatitude = boundyBox[0];
			double minLongitude = boundyBox[1];
			double maxLatitude = boundyBox[2];
			double maxLongitude = boundyBox[3];
			// Create boundy
			currentArrondissement = new Arrondissement(System.currentTimeMillis());
			currentArrondissement.setNumber("0");
			currentArrondissement.setMinLatitude(minLatitude);
			currentArrondissement.setMinLongitude(minLongitude);
			currentArrondissement.setMaxLatitude(maxLatitude);
			currentArrondissement.setMaxLongitude(maxLongitude);
			arrondissements.add(currentArrondissement);
		}
		return arrondissements;
	}

}
