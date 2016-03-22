import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.routing.AlgorithmOptions;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint3D;

import java.awt.geom.Point2D;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Locale;

/**
 * Created by lukasz on 27.10.15.
 */
public class OptimalRouteCoverageCalc {

    private static final String OSM_FILE_PATH
            = "/home/lukasz/Pulpit/Magisterskie/Praca_magisterska/GraphHopper/wielkopolskie-latest.osm.pbf";
    private static final String CH_GRAPH_FOLDER
            = "/home/lukasz/Pulpit/Magisterskie/Praca_magisterska/GraphHopper/Dev/GraphHopper_routing/generated_ch_graph";
    private static final String GRAPH_FOLDER
            = "/home/lukasz/Pulpit/Magisterskie/Praca_magisterska/GraphHopper/Dev/GraphHopper_routing/generated_graph";

    /*
    latitude: 1 degree ~ 111 km
    longitude: long = 111km * cos(lat) (45th degree) ~ 78km
     */
    private static final double LAT_MARGIN = 0.0005; // ~ 100m
    private static final double LONG_MARGIN = 0.0007; // ~ 100m


    public PointList findOptimalRoute(Point2D.Double startPoint, Point2D.Double finishPoint, String chosenWeighting) {

        MyGraphHopper hopper = new MyGraphHopper();
        hopper.setOSMFile(OSM_FILE_PATH);

        hopper.setGraphHopperLocation(GRAPH_FOLDER);
        hopper.setEncodingManager(new EncodingManager("car"));
        hopper.setCHEnable(false);
        hopper.importOrLoad();

        //System.out.println("StartPoint - Latitude: " + startPoint.getX() + ", Long: " + startPoint.getY());
        //System.out.println("EndPoint - Latitude: " + finishPoint.getX() + ", Long: " + finishPoint.getY());

        GHRequest req = new GHRequest(startPoint.getX(), startPoint.getY(), finishPoint.getX(), finishPoint.getY())
                .setWeighting(chosenWeighting)
                .setVehicle("car")
                .setLocale(Locale.US)
                .setAlgorithm(AlgorithmOptions.DIJKSTRA_BI);

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
        double weightedCounter = 0;
        double routeLength = 0;
        for (int i = 1; i < realRoute.size(); i++) {
            Point2D.Double point = realRoute.get(i);
            double pointLat = point.getX();
            double pointLong = point.getY() * Math.cos(Math.toRadians(pointLat));

            Point2D.Double prevPoint = realRoute.get(i - 1);
            double prevPointLat = prevPoint.getX();
            double prevPointLong = prevPoint.getY() * Math.cos(Math.toRadians(prevPointLat));

            double distanceFromPrevPoint = Math.sqrt(Math.pow(pointLat - prevPointLat, 2)
                    + Math.pow((pointLong - prevPointLong), 2));
            routeLength += distanceFromPrevPoint;

            double distance = -1;

            for (int ii = 0; ii < optimalRoute.size() - 1; ii++) {
                double beginningPointLat = optimalRoute.getLat(ii);
                double beginningPointLong = optimalRoute.getLon(ii) * Math.cos(Math.toRadians(beginningPointLat));

                double endPointLat = optimalRoute.getLat(ii + 1);
                double endPointLong = optimalRoute.getLon(ii + 1) * Math.cos(Math.toRadians(endPointLat));

                //line given by equation ax + by + c = 0
                double a = beginningPointLat - endPointLat;
                double b = endPointLong - beginningPointLong;
                double c = endPointLat * beginningPointLong - beginningPointLat * endPointLong;

                //line vertical to given line: (-b/a)x + y - d = 0
                double a2 = (-1) * b / a;
                double d = pointLat + a2 * pointLong;

                //determinants
                double w = a - b * a2;
                double wx = (-1) * c - b * d;
                double wy = a * d + c * a2;

                if (w != 0) {
                    double x = wx / w;
                    double y = wy / w;

                    //check if point is on defined line segment
                    if ((x >= endPointLong && x <= beginningPointLong ||
                            x >= beginningPointLong && x <= endPointLong)
                            &&
                            (y >= endPointLat && y <= beginningPointLat ||
                                y >= beginningPointLat && y <= endPointLat)) {

                        //then calculate the distance from point describing real route to the line describing optimal
                        // route
                        double tempDistance = Math.abs(a * pointLong + b * pointLat + c)
                                / Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));

                        if (distance == -1 || tempDistance < distance) {
                            distance = tempDistance;
                        }

                    }
                }
            }
            if (distance != -1 && distance <= LAT_MARGIN) {
                counter++;
                weightedCounter += distanceFromPrevPoint;
            }
        }
        try {
            PrintWriter printWriter = new PrintWriter(new FileWriter(Consts.OPTIMAL_COVERAGE_RESULTS_FILE, true));
            printWriter.println(weightedCounter / routeLength + " " + routeLength);
            printWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Optimal route weighted coverage: " + weightedCounter / routeLength);
        System.out.println("Number of points on optimal route: " + counter + "/" + realRoute.size());
        return (double) counter/realRoute.size();
    }

}
