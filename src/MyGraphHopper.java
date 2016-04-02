import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.Weighting;
import com.graphhopper.routing.util.WeightingMap;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.util.EdgeIteratorState;

import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by lukasz on 22.03.16.
 */
public class MyGraphHopper extends GraphHopper {

    @Override
    public Weighting createWeighting(WeightingMap weightingMap, FlagEncoder encoder) {
        String weighting = weightingMap.getWeighting();
        if (Consts.CURRENT_TRAFFIC.equalsIgnoreCase(weighting)) {
            return new CurrentTrafficWeighting(encoder);
        } else {
            return super.createWeighting(weightingMap, encoder);
        }
    }

    public void loadTrafficData() {
        LocationIndex locationIndex = getLocationIndex();
        FlagEncoder carEncoder = getEncodingManager().getEncoder("car");

        DataReader reader = new DataReader();
        for (int id = 1; id < 100; id++) {
            Set modifiedEdges = new HashSet();
            java.util.List<Point2D.Double> route = reader.readDb(id, Consts.TRAFFIC_TABLE);
            for (Point2D.Double point : route) {
                double latitude = point.getX();
                double longitude = point.getY();
                QueryResult qr = locationIndex.findClosest(latitude, longitude, EdgeFilter.ALL_EDGES);
                EdgeIteratorState edge = qr.getClosestEdge();
                int edgeId = edge.getEdge();
                if (!modifiedEdges.contains(edgeId)) {
                    modifiedEdges.add(edgeId);
                    long existingFlags = edge.getFlags();
                    double oldSpeed = carEncoder.getSpeed(existingFlags);
                    if (oldSpeed > 5) {
                        double newSpeed = oldSpeed - 5;
                        edge.setFlags(carEncoder.setSpeed(existingFlags, newSpeed));
                        System.out.println("Edge " + edgeId + " speed changed from " + oldSpeed + " to " + newSpeed);
                    }
                }


            }
        }
    }
}
