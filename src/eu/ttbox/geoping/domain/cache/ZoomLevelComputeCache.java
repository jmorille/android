package eu.ttbox.geoping.domain.cache;

import microsoft.mappoint.TileSystem;

public class ZoomLevelComputeCache {

    private double latitude;
    private int cachedZoomLevel = -1;
    private float cachedZoomLevelGroundResolutionInM = -1;

    public ZoomLevelComputeCache(double latitude) {
        super();
        this.latitude = latitude;
    }

    public float computeGroundResolutionInMForZoomLevel(int zoomLevel) {
        if (cachedZoomLevel != zoomLevel) {
            cachedZoomLevelGroundResolutionInM = (float) TileSystem.GroundResolution(latitude, zoomLevel);
            cachedZoomLevel = zoomLevel;
        }
        return cachedZoomLevelGroundResolutionInM;
    }

}
