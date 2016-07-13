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
        FlagEncoder customCarEncoder = getEncodingManager().getEncoder(CustomEncodingManager.CUSTOM_CAR);
        //FlagEncoder customCarEncoder = getEncodingManager().getEncoder("car");
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
                double oldSpeed = customCarEncoder.getSpeed(existingFlags);
                double newSpeed;
                Integer numberOfSamples = modifiedEdges.get(edgeId);
                if (numberOfSamples == null) {
                    newSpeed = (2 * oldSpeed + speed) / 3;
                    modifiedEdges.put(edgeId, 3);
                } else {
                    if (!Consts.CONSIDER_TRAFFIC_FLAG) {
                        System.out.println("Old speed: " + oldSpeed);
                        oldSpeed = customCarEncoder.getDouble(existingFlags,
                                CustomCarFlagEncoder.CUSTOM_SPEED_KEY);
                        System.out.println("Old custom speed: " + oldSpeed);
                    }
                    newSpeed = oldSpeed + speed / ++numberOfSamples;
                    modifiedEdges.put(edgeId, numberOfSamples);
                }
                if (Consts.CONSIDER_TRAFFIC_FLAG) {
                    edge.setFlags(customCarEncoder.setSpeed(existingFlags, newSpeed));
                    System.out.println("Edge " + edgeId + " speed changed from " + oldSpeed + " to " + newSpeed +
                            ", number of samples: " + numberOfSamples);
                }
                if (Consts.STORE_EXTRA_CUSTOM_SPEED) {
                    if (Consts.CONSIDER_TRAFFIC_FLAG) {
                        existingFlags = edge.getFlags();
                    }
                    edge.setFlags(customCarEncoder.setDouble(existingFlags, CustomCarFlagEncoder.CUSTOM_SPEED_KEY,
                            newSpeed));
                    System.out.println("Edge " + edgeId + " - custom speed changed from "
                            + oldSpeed + " to " + newSpeed +
                            ", number of samples: " + numberOfSamples);
                }

                //TEST
//                long testExistingFlags = edge.getFlags();
//                double testSpeed = customCarEncoder.getSpeed(testExistingFlags);
//                double testCustomSpeed = customCarEncoder.getDouble(testExistingFlags, CustomCarFlagEncoder.CUSTOM_SPEED_KEY);
//                assert testSpeed == testCustomSpeed;
            }
        }
    }
}
