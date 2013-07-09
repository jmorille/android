package eu.ttbox.velib.model;

import java.util.List;

public class StationSearchResult {

	List<Station> stations;

	int totalResult;

	int start;

	int pageSize;

	public StationSearchResult() {
		super();
	}

	public StationSearchResult(List<Station> stations, int totalResult, int start, int pageSize) {
		super();
		this.stations = stations;
		this.totalResult = totalResult;
		this.pageSize = pageSize;
		this.start = start;
	}

	public List<Station> getStations() {
		return stations;
	}

	public void setStations(List<Station> stations) {
		this.stations = stations;
	}

	public int getTotalResult() {
		return totalResult;
	}

	public void setTotalResult(int totalResult) {
		this.totalResult = totalResult;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

}
