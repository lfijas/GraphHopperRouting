import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.util.EdgeIteratorState;

/**
 * Created by lukasz on 09.04.17.
 */
public class Bridge {

    private long id;
    private String name;
    private double latitude;
    private double longitude;

    public Bridge(long id, String name, double latitude, double longitude) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getBridgeAsEdge(MyGraphHopper hopper) {
        LocationIndex locationIndex = hopper.getLocationIndex();
        QueryResult qr = locationIndex.findClosest(latitude, longitude, EdgeFilter.ALL_EDGES);
        EdgeIteratorState edge = qr.getClosestEdge();
        return edge.getEdge();
    }

}
