import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.routing.AlgorithmOptions;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint3D;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.Locale;

/**
 * Created by lukasz on 27.10.15.
 */
public class OptimalRouteCoverageCalc {

    private static final String OSM_FILE_PATH = "/home/lukasz/Pulpit/Magisterskie/Praca_magisterska/GraphHopper/wielkopolskie-latest.osm.pbf";
    private static final String CH_GRAPH_FOLDER = "/home/lukasz/Pulpit/Magisterskie/Praca_magisterska/GraphHopper/Dev/GraphHopper_routing/generated_ch_graph";
    private static final String GRAPH_FOLDER = "/home/lukasz/Pulpit/Magisterskie/Praca_magisterska/GraphHopper/Dev/GraphHopper_routing/generated_graph";

    /*
    latitude: 1 degree ~ 111 km
    longitude: (45th degree) ~ 78km
     */
    private static final double LAT_MARGIN = 0.0005; // ~ 100m
    private static final double LONG_MARGIN = 0.0007; // ~ 100m


    public PointList findOptimalRoute(Point2D.Double startPoint, Point2D.Double finishPoint) {

        GraphHopper hopper = new GraphHopper().forServer();
        hopper.setOSMFile(OSM_FILE_PATH);

        hopper.setGraphHopperLocation(CH_GRAPH_FOLDER);
        hopper.setEncodingManager(new EncodingManager("car"));
        hopper.importOrLoad();

        //System.out.println("StartPoint - Latitude: " + startPoint.getX() + ", Long: " + startPoint.getY());
        //System.out.println("EndPoint - Latitude: " + finishPoint.getX() + ", Long: " + finishPoint.getY());

        GHRequest req = new GHRequest(startPoint.getX(), startPoint.getY(), finishPoint.getX(), finishPoint.getY())
                .setWeighting("fastest")
                .setVehicle("car")
                .setLocale(Locale.US)
                .setAlgorithm(AlgorithmOptions.ASTAR_BI);

        GHResponse rsp = hopper.route(req);

        if(rsp.hasErrors()) {
            System.out.println("Errors!!!");
            return null;
        }

        PointList pointList = rsp.getPoints();
        double distance = rsp.getDistance();
        long timeInMs = rsp.getTime();

        //System.out.println("AStar bi - Distance: " + distance + ", time in millis: " + timeInMs);
        //System.out.println("Points:");
        for (GHPoint3D point : pointList) {
            double latitude = point.getLat();
            double longitude = point.getLon();
            //System.out.println("Lat: " + latitude + ", Long: " + longitude);

        }
        return pointList;
    }

    public double calculateOptimalRouteCoverage(List<Point2D.Double> realRoute, PointList optimalRoute) {
        int counter = 1;
        for (int i = 1; i < realRoute.size(); i++) {
            Point2D.Double point = realRoute.get(i);
            double pointLat = point.getX();
            double pointLong = point.getY();
            boolean isEndOptimalRoute = true;
            for (GHPoint3D p : optimalRoute) {
                double pLat = p.getLat();
                double pLong = p.getLon();
                if (pointLat >= pLat - LAT_MARGIN && pointLat <= pLat + LAT_MARGIN
                        && pointLong >= pLong- LONG_MARGIN && pointLong <= pLong + LONG_MARGIN) {
                    counter++;
                    isEndOptimalRoute = false;
                    break;
                }
            }
//            if (isEndOptimalRoute) {;
//                break;
//            }
        }
        //System.out.println("Number of points on optimal route: " + counter + "/" + realRoute.size());
        return (double) counter/realRoute.size();
    }

}
