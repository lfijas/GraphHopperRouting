import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.routing.AlgorithmOptions;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint3D;

import java.awt.geom.Point2D;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by lukasz on 27.10.15.
 */
public class OptimalRouteCoverageCalc {

    /*
    latitude: 1 degree ~ 111 km
    longitude: long = 111km * cos(lat) (45th degree) ~ 78km
     */
    private static final double LAT_MARGIN = 0.0005; // ~ 100m
    private static final double LONG_MARGIN = 0.0007; // ~ 100m
    private static final double DEGREE_TO_KILOMETERS_FACTOR = 111.196672;


    public OptimalRoute findOptimalRoute(Point2D.Double startPoint, Point2D.Double finishPoint, String chosenWeighting,
                                         MyGraphHopper hopper) {

        //System.out.println("StartPoint - Latitude: " + startPoint.getX() + ", Long: " + startPoint.getY());
        //System.out.println("EndPoint - Latitude: " + finishPoint.getX() + ", Long: " + finishPoint.getY());

        GHRequest req = new GHRequest(startPoint.getX(), startPoint.getY(), finishPoint.getX(), finishPoint.getY())
                .setWeighting(chosenWeighting)
                .setVehicle(CustomEncodingManager.CUSTOM_CAR)
                .setLocale(Locale.US)
                .setAlgorithm(AlgorithmOptions.DIJKSTRA_BI);

        GHResponse rsp = hopper.route(req);

        if (rsp.hasErrors()) {
            System.out.println("Errors!!!");
            List<Throwable> errorList = rsp.getErrors();
            System.out.println("errorList size: " + errorList.size());
            for (Throwable e : errorList) {
                e.printStackTrace();
            }
            return null;
        }

        PointList pointList = rsp.getPoints();
        double distance = rsp.getDistance();
        long timeInMs = rsp.getTime();
        OptimalRoute optimalRoute = new OptimalRoute(pointList, timeInMs, distance);

        //System.out.println("AStar bi - Distance: " + distance + ", time in millis: " + timeInMs);
        //System.out.println("Points:");
        for (GHPoint3D point : pointList) {
            double latitude = point.getLat();
            double longitude = point.getLon();
            //System.out.println("Lat: " + latitude + ", Long: " + longitude);

        }
        return optimalRoute;
    }

    public double calculateOptimalRouteCoverage(int id, List<PositionWithTimeData> realRoute, PointList optimalRoute,
                                                long optimalRouteTime, double optimalRouteLength,
                                                String resultsFileName) {
        int counter = 1;
        double weightedCounter = 0;
        double routeLength = 0;
        for (int i = 1; i < realRoute.size(); i++) {
            Point2D.Double point = new Point2D.Double(realRoute.get(i).getLatitude(), realRoute.get(i).getLongitude());
            double pointLat = point.getX();
            double pointLong = point.getY() * Math.cos(Math.toRadians(pointLat));

            Point2D.Double prevPoint = new Point2D.Double(realRoute.get(i - 1).getLatitude(),
                    realRoute.get(i - 1).getLongitude());
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
            PrintWriter printWriter = new PrintWriter(new FileWriter(resultsFileName, true));
            printWriter.print(id + " ");
            printWriter.print(weightedCounter / routeLength + " "); //coverage ratio
            printWriter.print(weightedCounter * DEGREE_TO_KILOMETERS_FACTOR + " "); //coverage in km
            printWriter.print(routeLength * DEGREE_TO_KILOMETERS_FACTOR + " "); //route length in km
            printWriter.print(optimalRouteTime + " ");
            printWriter.print(optimalRouteLength / 1000 + " "); //optimal route length in km
            printWriter.print(realRoute.get(0).getTimestamp() + " "); //start time
            printWriter.print(realRoute.get(realRoute.size() - 1).getTimestamp()); //end time
            printWriter.println();
            printWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Optimal route weighted coverage: " + weightedCounter / routeLength);
        System.out.println("Number of points on optimal route: " + counter + "/" + realRoute.size());
        return (double) counter / realRoute.size();
    }

    public void findBridgesOnTheRoute(int id, List<PositionWithTimeData> realRoute, PointList optimalRoute,
                                      long optimalRouteTime, double optimalRouteLength, MyGraphHopper hopper,
                                      String realRouteResultsFileName, String optimalRouteResultsFileName,
                                      boolean isTrafficConsidered) {

        ArrayList<Bridge> bridgesInWarsaw = Utils.getWarsawBridges();
        LocationIndex locationIndex = hopper.getLocationIndex();

        if (!isTrafficConsidered) {
            //bridges on real route
            for (int i = 0; i < realRoute.size() - 3 ; i++) {

                double beginningPointLat = realRoute.get(i).getLatitude();
                double beginningPointLong = realRoute.get(i).getLongitude()
                        * Math.cos(Math.toRadians(beginningPointLat));

                double endPointLat = realRoute.get(i + 3).getLatitude();
                double endPointLong = realRoute.get(i + 3).getLongitude() * Math.cos(Math.toRadians(endPointLat));

                for (Bridge bridge : bridgesInWarsaw) {

                    double bridgeLat = bridge.getLatitude();
                    double bridgeLong = bridge.getLongitude() * Math.cos(Math.toRadians(bridgeLat));

                    double distance = castPointOnEdge(beginningPointLat, beginningPointLong, endPointLat, endPointLong,
                            bridgeLat, bridgeLong);
                    if (distance != -1 && distance <= LAT_MARGIN) {
                        try {
                            PrintWriter printWriter = new PrintWriter(new FileWriter(realRouteResultsFileName, true));
                            printWriter.print(id + " "); //route id
                            printWriter.print(bridge.getId() + " "); //found bridge id
                            printWriter.print(realRoute.get(0).getTimestamp() + " "); //start time
                            printWriter.print(realRoute.get(i).getTimestamp() + " "); //timestamp on the bridge
                            printWriter.print(realRoute.get(realRoute.size() - 1).getTimestamp()); //end time
                            printWriter.println();
                            printWriter.close();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

//                QueryResult qr = locationIndex.findClosest(realRoute.get(i).getLatitude(),
//                        realRoute.get(i).getLongitude(), EdgeFilter.ALL_EDGES);
//                int edgeId = qr.getClosestEdge().getEdge();
//                for (Bridge bridge : bridgesInWarsaw) {
//                    int bridgeEdgeId = bridge.getBridgeAsEdge(hopper);
//                    if (edgeId == bridgeEdgeId) {
//                        System.out.println("id: " + id + ", bridge id: " + bridge.getId() + ", timestamp: "
//                                + realRoute.get(i).getTimestamp());
//                        try {
//                            PrintWriter printWriter = new PrintWriter(new FileWriter(realRouteResultsFileName, true));
//                            printWriter.print(id + " "); //route id
//                            printWriter.print(bridge.getId() + " "); //found bridge id
//                            printWriter.print(realRoute.get(i).getTimestamp()); //timestamp
//                            printWriter.println();
//                            printWriter.close();
//                        } catch (FileNotFoundException e) {
//                            e.printStackTrace();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                        break;
//                    }
//                }
            }
        }

        //bridges on optimal route
        for (int i = 0; i < optimalRoute.size() - 1; i++) {

            double beginningPointLat = optimalRoute.getLat(i);
            double beginningPointLong = optimalRoute.getLon(i) * Math.cos(Math.toRadians(beginningPointLat));

            double endPointLat = optimalRoute.getLat(i + 1);
            double endPointLong = optimalRoute.getLon(i + 1) * Math.cos(Math.toRadians(endPointLat));

            for (Bridge bridge : bridgesInWarsaw) {

                double bridgeLat = bridge.getLatitude();
                double bridgeLong = bridge.getLongitude() * Math.cos(Math.toRadians(bridgeLat));

                double distance = castPointOnEdge(beginningPointLat, beginningPointLong, endPointLat, endPointLong,
                        bridgeLat, bridgeLong);
                if (distance != -1 && distance <= LAT_MARGIN) {
                    try {
                        PrintWriter printWriter = new PrintWriter(new FileWriter(optimalRouteResultsFileName, true));
                        printWriter.print(id + " "); //route id
                        printWriter.print(bridge.getId() + " "); //found bridge id
                        printWriter.print(optimalRouteTime + " ");
                        printWriter.print(optimalRouteLength / 1000 + " "); //optimal route length in km
                        printWriter.println();
                        printWriter.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

//            QueryResult qr = locationIndex.findClosest(optimalRoute.getLat(i),
//                    optimalRoute.getLon(i), EdgeFilter.ALL_EDGES);
//            int edgeId = qr.getClosestEdge().getEdge();
//            for (Bridge bridge : bridgesInWarsaw) {
//                int bridgeEdgeId = bridge.getBridgeAsEdge(hopper);
//                if (edgeId == bridgeEdgeId) {
//                    System.out.println("id: " + id + ", bridge id: " + bridge.getId());
//                    try {
//                        PrintWriter printWriter = new PrintWriter(new FileWriter(optimalRouteResultsFileName, true));
//                        printWriter.print(id + " "); //route id
//                        printWriter.print(bridge.getId() + " "); //found bridge id
//                        printWriter.println();
//                        printWriter.close();
//                    } catch (FileNotFoundException e) {
//                        e.printStackTrace();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    break;
//                }
//            }
        }

    }

    private double castPointOnEdge(double beginningPointLat, double beginningPointLong, double endPointLat,
                                   double endPointLong, double pointLat, double pointLong) {

        double distance = -1;

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
                distance = Math.abs(a * pointLong + b * pointLat + c)
                        / Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
            }
        }
        return distance;
    }

    public void compareOptimalRoutesTime(List<Point2D.Double> route, String chosenWeighting,
                                         MyGraphHopper hopper) {

        Point2D.Double startPoint = route.get(0);
        Point2D.Double finishPoint = route.get(route.size() - 1);
        OptimalRoute optimalRoute = findOptimalRoute(startPoint, finishPoint, chosenWeighting, hopper);
        System.out.println("Optimal route time: " + optimalRoute.getTime()
                + " number of points: " + optimalRoute.getRoute().size());
/*        long calculatedOptimalRouteTime = 0;
        for (int i = 1; i < route.size(); i++) {
            System.out.println("Optimal route part: " + i);
            OptimalRoute partOfOptimalRoute = findOptimalRoute(route.get(i - 1), route.get(i), chosenWeighting, hopper);
            calculatedOptimalRouteTime += partOfOptimalRoute.getTime();
            for (GHPoint3D point: partOfOptimalRoute.getRoute()) {
                System.out.println("Lat: " + point.getLat() + ", lon: " + point.getLon());
            }
        }
        System.out.println("Calculated optimal route time: " + calculatedOptimalRouteTime);*/

    }

}
