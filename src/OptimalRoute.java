import com.graphhopper.util.PointList;

/**
 * Created by lukasz on 13.06.16.
 */
public class OptimalRoute {

    private PointList route;
    private long time;

    public OptimalRoute(PointList route, long time) {
        this.route = route;
        this.time = time;
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
}
