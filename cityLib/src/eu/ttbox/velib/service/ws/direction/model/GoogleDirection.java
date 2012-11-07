package eu.ttbox.velib.service.ws.direction.model;

import java.util.ArrayList;

import eu.ttbox.velib.service.ws.direction.parser.GoogleDirectionStatusEnum;

public class GoogleDirection {

	public GoogleDirectionStatusEnum status;

	public ArrayList<GoogleDirectionRoute> routes = new ArrayList<GoogleDirectionRoute>();

	public ArrayList<GoogleDirectionRoute> getRoutes() {
		return routes;
	}

	public void addRoutes(GoogleDirectionRoute route) {
		this.routes.add(route);
	}

}
