package eu.ttbox.velib.service.ws.forecast;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

/**
 * Google Forecast Parser for service @see http://www.google.com/ig/api?weather=paris
 */
public class ForecastHandler extends DefaultHandler {
	/**
	 * The boolean to know if we are in a Forecast block
	 */
	boolean inInfoBlock = false;
	boolean inCurrentBlock = false;
	boolean inForecastBlock = false;
	/**
	 * The list of Forecast
	 */
	List<Forecast> forecasts;
	/**
	 * The current forecast being parse and built
	 */
	Forecast currentforecast;
	/**
	 * The current value of the element
	 */
	String value;
	/**
	 * The tag to use for the LogCat
	 */
	String tag = "ForecastHandler";
	/***********************/
	/** *** Constants *** **/
	/***********************/

	/**
	 * The string constant that are the name of the xml elements of the document
	 */
	private static final String COND = "condition", ICO = "icon", DATA = "data";
	private static final String INFO_ROOT = "forecast_information", INFO_CITY = "city", INFO_ZIP = "postal_code", INFO_LAT_E6 = "latitude_e6",
			INFO_LNG_E6 = "longitude_e6", INFO_DATE = "forecast_date", INFO_UNIT = "unit_system";
	private static final String CURRENT_ROOT = "current_conditions", CUR_TEMP_C = "temp_c", CUR_HUMIDITY = "humidity", CUR_WIND = "wind_condition";
	private static final String FORECAST_ROOT = "forecast_conditions", FOR_DAY = "day_of_week", FOR_LOW = "low", FOR_HIGH = "high";

	/*******************************************/
	/** *** Managing The Document parsing *** **/
	/*******************************************/

	@Override
	public void startDocument() throws SAXException {
		// instanciate the list of forecast you want to parse
		forecasts = new ArrayList<Forecast>();
	}

	/*******************************************************/
	/** *** Managing the begin and the end of a block *** **/
	/*******************************************************/

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		Log.d(tag, String.format("[startElement] uri : %s, localName : %s, qName: %s, Attribute size: ", uri, localName, qName, attributes.getLength()));
		value = attributes.getValue(DATA);
		if (inForecastBlock) {
			// so retrieve the value of the attribute
			value = attributes.getValue(DATA);
			// And set it to the right attribute of the forecast object:
			if (localName.equals(FOR_DAY)) {
				currentforecast.setDay(value);
			} else if (localName.equals(FOR_LOW)) {
				currentforecast.setLowTempInC(Integer.valueOf(value));
				// TODO F
			} else if (localName.equals(FOR_HIGH)) {
				currentforecast.setHighTempInC(Integer.valueOf(value));
				// TODO F
			} else if (localName.equals(ICO)) {
				currentforecast.setIconUrl(value);
			} else if (localName.equals(COND)) {
				currentforecast.setIconUrl(value);
			}
		} else if (localName.equals(FORECAST_ROOT)) {
			// then we begin a new forecast block, so instanciate the new forcastElement
			currentforecast = new Forecast();
			// We are in a ForeCastBlock
			inForecastBlock = true;
		} else if (localName.equals(INFO_ROOT)) {
			inInfoBlock = true;
		} else if (localName.equals(CURRENT_ROOT)) {
			inCurrentBlock = true;
		}

	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		Log.d(tag, String.format("[endElement] uri : %s, localName : %s, qName: %s", uri, localName, qName));
		if (localName.equals(FORECAST_ROOT)) {
			// then we begin a new forecast block, so instanciate the new forcastElement
			forecasts.add(currentforecast);
			inForecastBlock = false;
		} else if (localName.equals(INFO_ROOT)) {
			inInfoBlock = false;
		} else if (localName.equals(CURRENT_ROOT)) {
			inCurrentBlock = false;
		}
	}

	/******************************************************************************************/
	/** Managing the value within a block **************************************************************************/
	/******************************************************************************************/

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		// Calling when we're within an element.
		// In this Xml schema we are never in an element it's unused
	}

	/******************************************************************************************/
	/** Getter **************************************************************************/
	/******************************************************************************************/

	/**
	 * @return the forecasts
	 */
	public List<Forecast> getForecasts() {
		return forecasts;
	}

}
