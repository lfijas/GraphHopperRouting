import com.graphhopper.routing.util.*;
import com.graphhopper.util.PMap;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lukasz on 27.06.16.
 */
public class CustomEncodingManager extends EncodingManager {

    public static final String CUSTOM_CAR = "custom_car";

    public CustomEncodingManager(String flagEncodersStr) {
        this(flagEncodersStr, 4);
    }

    public CustomEncodingManager(String flagEncodersStr, int bytesForFlags )
    {
        this(parseEncoderString(flagEncodersStr), bytesForFlags);
    }

    public CustomEncodingManager(FlagEncoder... flagEncoders) {
        super(flagEncoders);
    }

    public CustomEncodingManager(List<? extends FlagEncoder> flagEncoders) {
        super(flagEncoders);
    }

    public CustomEncodingManager(List<? extends FlagEncoder> flagEncoders, int bytesForEdgeFlags) {
        super(flagEncoders, bytesForEdgeFlags);
    }

    static List<FlagEncoder> parseEncoderString(String encoderList )
    {
        if (encoderList.contains(":"))
            throw new IllegalArgumentException("EncodingManager does no longer use reflection instantiate encoders directly.");

        String[] entries = encoderList.split(",");
        List<FlagEncoder> resultEncoders = new ArrayList<FlagEncoder>();

        for (String entry : entries)
        {
            entry = entry.trim().toLowerCase();
            if (entry.isEmpty())
                continue;

            String entryVal = "";
            if (entry.contains("|"))
            {
                entryVal = entry;
                entry = entry.split("\\|")[0];
            }
            PMap configuration = new PMap(entryVal);

            AbstractFlagEncoder fe;
            if (entry.equals(CAR))
                fe = new CarFlagEncoder(configuration);

            else if (entry.equals(BIKE))
                fe = new BikeFlagEncoder(configuration);

            else if (entry.equals(BIKE2))
                fe = new Bike2WeightFlagEncoder(configuration);

            else if (entry.equals(RACINGBIKE))
                fe = new RacingBikeFlagEncoder(configuration);

            else if (entry.equals(MOUNTAINBIKE))
                fe = new MountainBikeFlagEncoder(configuration);

            else if (entry.equals(FOOT))
                fe = new FootFlagEncoder(configuration);

            else if (entry.equals(MOTORCYCLE))
                fe = new MotorcycleFlagEncoder(configuration);

            else if (entry.equals(CUSTOM_CAR)) {
                fe = new CustomCarFlagEncoder(configuration);
            }

            else
                throw new IllegalArgumentException("entry in encoder list not supported " + entry);

            if (configuration.has("version"))
            {
                if (fe.getVersion() != configuration.getInt("version", -1))
                {
                    throw new IllegalArgumentException("Encoder " + entry + " was used in version "
                            + configuration.getLong("version", -1) + ", but current version is " + fe.getVersion());
                }
            }

            resultEncoders.add(fe);
        }
        return resultEncoders;
    }

}
