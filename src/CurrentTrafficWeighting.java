import com.graphhopper.routing.util.AbstractWeighting;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.util.EdgeIteratorState;

/**
 * Created by lukasz on 22.03.16.
 */
public class CurrentTrafficWeighting extends AbstractWeighting {

    protected final static double SPEED_CONV = 3.6;

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

        double speed = reverse ? flagEncoder.getReverseSpeed(edgeState.getFlags()) : flagEncoder.getSpeed(edgeState.getFlags());
        if (speed == 0)
            return Double.POSITIVE_INFINITY;

        double time = edgeState.getDistance() / speed * SPEED_CONV;

        return time;

        //return edgeState.getDistance();
    }

    @Override
    public String getName() {
        return Consts.CURRENT_TRAFFIC;
    }
}
