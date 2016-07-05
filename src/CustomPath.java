import com.graphhopper.routing.Path;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.Graph;

/**
 * Created by lukasz on 28.06.16.
 */
public class CustomPath extends Path {

    protected long customTime;

    public CustomPath(Graph graph, FlagEncoder encoder) {
        super(graph, encoder);
    }

    @Override
    protected long calcMillis(double distance, long flags, boolean revert) {

        FlagEncoder customCarFlagEncoder = GraphHopperRoutingMain.getHopperInstance().getEncodingManager()
                .getEncoder(CustomEncodingManager.CUSTOM_CAR);

        if (revert && !customCarFlagEncoder.isBackward(flags)
                || !revert && !customCarFlagEncoder.isForward(flags))
            throw new IllegalStateException("Calculating time should not require to read speed from edge in wrong direction. "
                    + "Reverse:" + revert + ", fwd:" + customCarFlagEncoder.isForward(flags) + ", bwd:" + customCarFlagEncoder.isBackward(flags));

        double speed = revert ? customCarFlagEncoder.getReverseSpeed(flags) : customCarFlagEncoder.getSpeed(flags);
        if (Double.isInfinite(speed) || Double.isNaN(speed) || speed < 0)
            throw new IllegalStateException("Invalid speed stored in edge! " + speed);

        if (speed == 0)
            throw new IllegalStateException("Speed cannot be 0 for unblocked edge, use access properties to mark edge blocked! Should only occur for shortest path calculation. See #242.");

        customTime += (long) (distance * 3600 / speed);

        return super.calcMillis(distance, flags, revert);

    }
}
