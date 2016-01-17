import com.graphhopper.util.PointList;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Created by lukasz on 13.10.15.
 */
public class GraphHopperRoutingMain {

    private static final String OSM_FILE_PATH = "/home/lukasz/Pulpit/Magisterskie/Praca_magisterska/GraphHopper/wielkopolskie-latest.osm.pbf";
    private static final String CH_GRAPH_FOLDER = "/home/lukasz/Pulpit/Magisterskie/Praca_magisterska/GraphHopper/GraphHopper_dev/graphhopper/core/generated_ch_graph";
    private static final String GRAPH_FOLDER = "/home/lukasz/Pulpit/Magisterskie/Praca_magisterska/GraphHopper/GraphHopper_dev/graphhopper/core/generated_graph";

    public static void main(String[] args) {

        System.out.println("Program started");

        final JFrame frame = new JFrame("OptimalRouteFinder");

        JLabel label = new JLabel("Id przejazdu: ");
        final JTextField textField = new JTextField(10);
        JPanel inputDataPanel = new JPanel();
        inputDataPanel.add(label);
        inputDataPanel.add(textField);

        JButton button = new JButton("Find Route");
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(button);

        JButton calculateBtn = new JButton("Calculate");
        JPanel calculateBtnPanel = new JPanel();
        calculateBtnPanel.add(calculateBtn);



        JLabel infoLabel = new JLabel("Zakres id przejazdów: od 1 do 42829");
        JPanel infoPanel = new JPanel();
        infoPanel.add(infoLabel);

        //frame.getContentPane().add(label);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setSize(300, 150);
        frame.setVisible(true);
        frame.setLayout(new GridLayout(4, 1));

        frame.add(inputDataPanel);
        frame.add(infoPanel);
        frame.add(buttonPanel);
        frame.add(calculateBtnPanel);

        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if ("".equals(textField.getText())) {
                    JOptionPane.showMessageDialog(frame, "Proszę podać id przejazdu");
                }
                else {
                    int id = 0;
                    try {
                        id = Integer.parseInt(textField.getText());
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(frame, "Proszę podać poprawne id przejazdu");
                    }


                    DataReader reader = new DataReader();
                    OptimalRouteCoverageCalc optimalRouteCoverageCalc = new OptimalRouteCoverageCalc();

                    List<Point2D.Double> route = reader.readDb(id);

                    if (route.size() > 0) {
                        Point2D.Double startPoint = route.get(0);
                        Point2D.Double finishPoint = route.get(route.size() - 1);

                        PointList optimalRoute = optimalRouteCoverageCalc.findOptimalRoute(startPoint, finishPoint);
                        if (optimalRoute != null) {
                            reader.saveOptimalRouteIntoDb(id, optimalRoute);
                            double pointsCoverage = optimalRouteCoverageCalc.calculateOptimalRouteCoverage(route, optimalRoute);
                            System.out.println("Route #" + id + " - pointsCoverage: " + pointsCoverage);
                            try {
                                Desktop.getDesktop().browse(new URI("http://localhost/map.html?id=" + id));
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (URISyntaxException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        });

        calculateBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                DataReader reader = new DataReader();
                OptimalRouteCoverageCalc optimalRouteCoverageCalc = new OptimalRouteCoverageCalc();

                for (int id = 1; id < 1000; id++) {

                    List<Point2D.Double> route = reader.readDb(id);

                    if (route.size() > 0) {
                        Point2D.Double startPoint = route.get(0);
                        Point2D.Double finishPoint = route.get(route.size() - 1);

                        PointList optimalRoute = optimalRouteCoverageCalc.findOptimalRoute(startPoint, finishPoint);
                        if (optimalRoute != null) {
                            reader.saveOptimalRouteIntoDb(id, optimalRoute);
                            System.out.println("Route #" + id);
                            double pointsCoverage = optimalRouteCoverageCalc.calculateOptimalRouteCoverage(route, optimalRoute);
                            System.out.println("PointsCoverage: " + pointsCoverage);
                        }
                    }
                }
            }
    });

        /*GraphHopper hopper = new GraphHopper().forServer();
        hopper.setOSMFile(OSM_FILE_PATH);

        hopper.setGraphHopperLocation(CH_GRAPH_FOLDER);
        hopper.setEncodingManager(new EncodingManager("car"));
        hopper.importOrLoad();

        GHRequest req = new GHRequest(52.413597, 16.819385, 52.344017, 17.073616)
                .setWeighting("fastest")
                .setVehicle("car")
                .setLocale(Locale.US)
                .setAlgorithm(AlgorithmOptions.ASTAR_BI);

        GHResponse rsp = hopper.route(req);

        if(rsp.hasErrors()) {
            System.out.println("Errors!!!");
            return;
        }

        PointList pointList = rsp.getPoints();
        double distance = rsp.getDistance();
        long timeInMs = rsp.getTime();

        System.out.println("AStar bi - Distance: " + distance + ", time in millis: " + timeInMs);

        req = new GHRequest(52.413597, 16.819385, 52.344017, 17.073616)
                .setWeighting("fastest")
                .setVehicle("car")
                .setLocale(Locale.US)
                .setAlgorithm(AlgorithmOptions.DIJKSTRA_BI);

        rsp = hopper.route(req);

        if(rsp.hasErrors()) {
            System.out.println("Errors!!!");
            return;
        }

        pointList = rsp.getPoints();
        distance = rsp.getDistance();
        timeInMs = rsp.getTime();

        System.out.println("Dijkstra bi - Distance: " + distance + ", time in millis: " + timeInMs);

        hopper = new GraphHopper().forServer();
        hopper.setCHEnable(false);
        hopper.setOSMFile(OSM_FILE_PATH);

        hopper.setGraphHopperLocation(GRAPH_FOLDER);
        hopper.setEncodingManager(new EncodingManager("car"));

        hopper.importOrLoad();

        req = new GHRequest(52.413597, 16.819385, 52.344017, 17.073616)
                .setWeighting("fastest")
                .setVehicle("car")
                .setLocale(Locale.US)
                .setAlgorithm(AlgorithmOptions.DIJKSTRA);

        rsp = hopper.route(req);

        if(rsp.hasErrors()) {
            System.out.println("Errors!!!");
            return;
        }

        pointList = rsp.getPoints();
        distance = rsp.getDistance();
        timeInMs = rsp.getTime();

        System.out.println("Dijkstra - Distance: " + distance + ", time in millis: " + timeInMs);

        req = new GHRequest(52.413597, 16.819385, 52.344017, 17.073616)
                .setWeighting("fastest")
                .setVehicle("car")
                .setLocale(Locale.US)
                .setAlgorithm(AlgorithmOptions.ASTAR);

        rsp = hopper.route(req);

        if(rsp.hasErrors()) {
            System.out.println("Errors!!!");
            return;
        }

        pointList = rsp.getPoints();
        distance = rsp.getDistance();
        timeInMs = rsp.getTime();

        System.out.println("AStar - Distance: " + distance + ", time in millis: " + timeInMs);

*/


    }

}
