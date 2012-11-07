package eu.ttbox.velib.service.ws.forecast;

public class Forecast {

	String day;
	Integer lowTemp;
	Integer highTemp;
	String iconUrl;

	/** Constructor **/
	/*****************/
	public Forecast() {
		super();
	}

	public Forecast(String day, Integer lowTemp, Integer highTemp, String iconUrl) {
		super();
		this.day = day;
		this.lowTemp = lowTemp;
		this.highTemp = highTemp;
		this.iconUrl = iconUrl;
	}

	/******************************************************************************************/
	/** Accessors **************************************************************************/
	/******************************************************************************************/

	public String getDay() {
		return day;
	}

	public void setDay(String day) {
		this.day = day;
	}

	public Integer getLowTemp() {
		return lowTemp;
	}

	public void setLowTempInC(Integer lowTemp) {
		this.lowTemp = lowTemp;
	}

	public void setLowTempInF(Integer lowTemp) {
		this.lowTemp = (lowTemp - 32) * 5 / 9;
	}

	public Integer getHighTemp() {
		return highTemp;
	}

	public void setHighTempInC(Integer highTemp) {
		this.highTemp = highTemp;
	}

	public void setHighTempInF(Integer highTemp) {
		this.highTemp = (highTemp - 32) * 5 / 9;
	}

	/**
	 * @return the iconUrl
	 */
	public String getIconUrl() {
		return iconUrl;
	}

	/**
	 * @param iconUrl
	 *            the iconUrl to set
	 */
	public void setIconUrl(String iconUrl) {
		this.iconUrl = String.format("http://www.google.com%s", iconUrl);
	}

}
