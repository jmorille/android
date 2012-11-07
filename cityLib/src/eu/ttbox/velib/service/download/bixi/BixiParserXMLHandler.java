package eu.ttbox.velib.service.download.bixi;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import eu.ttbox.velib.model.Arrondissement;
import eu.ttbox.velib.model.Station;

/**
 * @see https://profil.bixi.ca/data/bikeStations.xml
 * 
 */
public class BixiParserXMLHandler extends DefaultHandler {

	private final String ITEM = "station";
	private final String ITEM_ARRONDISSEMENTS = "arrondissements";
	private final String ITEM_ARRONDISSEMENT = "arrondissement ";
	private final String ATR_ARRONDISSEMENT_NUMBER = "number ";
	private final String ATR_ARRONDISSEMENT_MINLAT = "minLat ";
	private final String ATR_ARRONDISSEMENT_MAXLAT = "maxLat ";
	private final String ATR_ARRONDISSEMENT_MINLNG = "minLng ";
	private final String ATR_ARRONDISSEMENT_MAXLNG = "maxLng ";

	private final String ATTR_NAME = "name";
	private final String ATTR_NUMBER = "terminalName";
	private final String ATTR_LATITUDE = "lat";
	private final String ATTR_LONGITUDE = "long";
	// private final String ATTR_ADRESS = "address";
	// private final String ATTR_FULLADDRESS = "fullAddress";
	private final String ATTR_OPEN = "open";
	private final String ATTR_BONUS = "bonus";

	// Array list de feeds
	private ArrayList<Station> entries;
	// Boolean permettant de savoir si nous sommes à l'intérieur d'un item
	private boolean inItem;
	// Feed courant
	private Station currentFeed;

	private Arrondissement currentArrondissement;

	private int provider;

	@Override
	public void processingInstruction(String target, String data) throws SAXException {
		super.processingInstruction(target, data);
	}

	public BixiParserXMLHandler(int provider) {
		super();
		this.provider = provider;
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
			currentFeed.setProvider(provider);
			currentFeed.setNumber(attributes.getValue(ATTR_NUMBER));
			currentFeed.setName(attributes.getValue(ATTR_NAME));
			// currentFeed.setAddress(attributes.getValue(ATTR_ADRESS));
			// currentFeed.setFullAddress(attributes.getValue(ATTR_FULLADDRESS));
			currentFeed.setLatitude(Double.valueOf(attributes.getValue(ATTR_LATITUDE)));
			currentFeed.setLongitude(Double.valueOf(attributes.getValue(ATTR_LONGITUDE)));
			currentFeed.setOpen(parseBoolean(attributes.getValue(ATTR_OPEN)));
			currentFeed.setBonus(parseBoolean(attributes.getValue(ATTR_BONUS)));
			inItem = true;
		} else if (name.equalsIgnoreCase(ITEM_ARRONDISSEMENT)) {
			this.currentArrondissement = new Arrondissement(System.currentTimeMillis());
			currentArrondissement.setNumber(attributes.getValue(ATR_ARRONDISSEMENT_NUMBER));
			currentArrondissement.setMinLatitude(Double.valueOf(attributes.getValue(ATR_ARRONDISSEMENT_MINLAT)));
			currentArrondissement.setMinLongitude(Double.valueOf(attributes.getValue(ATR_ARRONDISSEMENT_MINLNG)));
			currentArrondissement.setMaxLatitude(Double.valueOf(attributes.getValue(ATR_ARRONDISSEMENT_MAXLAT)));
			currentArrondissement.setMaxLongitude(Double.valueOf(attributes.getValue(ATR_ARRONDISSEMENT_MAXLNG)));
		}

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
		}
	}

	// cette méthode nous permettra de récupérer les données
	public ArrayList<Station> getData() {
		return entries;
	}

	public Arrondissement getDataArrondissement() {
		if (currentArrondissement == null && entries != null && !entries.isEmpty()) {
			double minLatitude = Double.MAX_VALUE;
			double minLongitude = Double.MAX_VALUE;

			double maxLatitude = Double.MIN_VALUE;
			double maxLongitude = Double.MIN_VALUE;

			for (Station station : entries) {
				minLatitude = Math.min(minLatitude, station.getLatitude());
				minLongitude = Math.min(minLongitude, station.getLongitude());
				maxLatitude = Math.max(maxLatitude, station.getLatitude());
				maxLongitude = Math.max(maxLongitude, station.getLongitude());
			}
			currentArrondissement = new Arrondissement(System.currentTimeMillis());
			currentArrondissement.setNumber("0");
			currentArrondissement.setMinLatitude(minLatitude);
			currentArrondissement.setMinLongitude(minLongitude);
			currentArrondissement.setMaxLatitude(maxLatitude);
			currentArrondissement.setMaxLongitude(maxLongitude);
		}
		return currentArrondissement;
	}
}
