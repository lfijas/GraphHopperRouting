import com.graphhopper.routing.util.AbstractWeighting;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.util.EdgeIteratorState;

/**
 * Created by lukasz on 22.03.16.
 */
public class CurrentTrafficWeighting extends AbstractWeighting {

    public CurrentTrafficWeighting(FlagEncoder encoder) {
        super(encoder);
        System.out.println("Current traffic weighting");
    }

    @Override
    public double getMinWeight(double distance) {
        return distance;
    }

    @Override
    public double calcWeight(EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId) {
        return edgeState.getDistance();
    }

    @Override
    public String getName() {
        return Consts.CURRENT_TRAFFIC;
    }
}
