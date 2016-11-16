import com.graphhopper.util.PointList;

/**
 * Created by lukasz on 13.06.16.
 */
public class OptimalRoute {

    private PointList route;
    private long time;
    private double routeLength;

    public OptimalRoute(PointList route, long time, double routeLength) {
        this.route = route;
        this.time = time;
        this.routeLength = routeLength;
    }

    public PointList getRoute() {
        return route;
    }

    public void setRoute(PointList route) {
        this.route = route;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public double getRouteLength() {
        return routeLength;
    }

    public void setRouteLength(double routeLength) {
        this.routeLength = routeLength;
    }
}
