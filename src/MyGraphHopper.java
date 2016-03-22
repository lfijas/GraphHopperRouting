import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.Weighting;
import com.graphhopper.routing.util.WeightingMap;

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
}
