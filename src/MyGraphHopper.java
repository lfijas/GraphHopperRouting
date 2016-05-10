import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.Weighting;
import com.graphhopper.routing.util.WeightingMap;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.util.EdgeIteratorState;

import java.util.*;

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
        Map<Integer, Integer> modifiedEdges = new HashMap();

        DataReader reader = new DataReader();
        java.util.List<Integer> selectedRoutes = reader.readSelectedTrafficId(Consts.MORNING_TRAFFIC_QUERY);

        //for (int id = 1; id < 1000; id++) {
        for (int id : selectedRoutes) {
            List<FullTrafficData> fullTrafficDataList = reader.readFullTrafficData(id, Consts.TRAFFIC_WITH_SPEED_TABLE);
            for (FullTrafficData point : fullTrafficDataList) {
                double latitude = point.getLatitude();
                double longitude = point.getLongitude();
                int speed = point.getSpeed();
                QueryResult qr = locationIndex.findClosest(latitude, longitude, EdgeFilter.ALL_EDGES);
                EdgeIteratorState edge = qr.getClosestEdge();
                int edgeId = edge.getEdge();
                long existingFlags = edge.getFlags();
                double oldSpeed = carEncoder.getSpeed(existingFlags);
                double newSpeed;
                Integer numberOfSamples = modifiedEdges.get(edgeId);
                if (numberOfSamples == null) {
                    newSpeed = (2 * oldSpeed + speed) / 3;
                    modifiedEdges.put(edgeId, 3);
                } else {
                    newSpeed = oldSpeed + speed / ++numberOfSamples;
                    modifiedEdges.put(edgeId, numberOfSamples);
                }
                edge.setFlags(carEncoder.setSpeed(existingFlags, newSpeed));
                System.out.println("Edge " + edgeId + " speed changed from " + oldSpeed + " to " + newSpeed +
                        ", number of samples: " + numberOfSamples);
            }
        }
    }
}
