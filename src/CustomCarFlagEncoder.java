import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.EncodedDoubleValue;
import com.graphhopper.util.BitUtil;
import com.graphhopper.util.PMap;

/**
 * Created by lukasz on 21.06.16.
 */
public class CustomCarFlagEncoder extends CarFlagEncoder {

    public static final int CUSTOM_SPEED_KEY = 12345;

    private EncodedDoubleValue customSpeedEncoder;

    public CustomCarFlagEncoder() {
        super();
    }

    public CustomCarFlagEncoder(PMap properties) {
        super(properties);
        //maxPossibleSpeed = 170;
    }

    public CustomCarFlagEncoder(String propertiesStr) {
        super(propertiesStr);
    }

    public CustomCarFlagEncoder(int speedBits, double speedFactor, int maxTurnCosts) {
        super(speedBits, speedFactor, maxTurnCosts);
    }

    @Override
    public int defineWayBits(int index, int shift) {

        shift = super.defineWayBits(index, shift);
        customSpeedEncoder = new EncodedDoubleValue("Custom speed", shift, speedBits, speedFactor,
                defaultSpeedMap.get("secondary"), maxPossibleSpeed);
        shift += customSpeedEncoder.getBits();

        return shift;
    }

    @Override
    public long setSpeed(long flags, double speed) {
        customSpeedEncoder.setDoubleValue(flags, speed);
        return super.setSpeed(flags, speed);
    }

    @Override
    public double getDouble(long flags, int key) {
        switch (key) {
            case CUSTOM_SPEED_KEY:
                return customSpeedEncoder.getDoubleValue(flags);
            default:
                return super.getDouble(flags, key);
        }
    }

    @Override
    public long setDouble(long flags, int key, double value) {
        switch (key) {
            case CUSTOM_SPEED_KEY:
                if (value < 0 || Double.isNaN(value))
                    throw new IllegalArgumentException("Speed cannot be negative or NaN: " + value
                            + ", flags:" + BitUtil.LITTLE.toBitString(flags));

                if (value > getMaxSpeed())
                    value = getMaxSpeed();
                return customSpeedEncoder.setDoubleValue(flags, value);
            default:
                return super.setDouble(flags, key, value);
        }
    }

    @Override
    public String toString() {
        return CustomEncodingManager.CUSTOM_CAR;
    }
}
