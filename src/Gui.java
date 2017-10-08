import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by lukasz on 22.03.16.
 */
public class Gui {

    private JRadioButton mFastestRadioBtn;
    private JRadioButton mShortestRadioBtn;
    private JRadioButton mCustomTrafficRadioBtn;

    public Gui(final MyGraphHopper hopper) {

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

        ButtonGroup weightingButtonGroup = new ButtonGroup();
        mFastestRadioBtn = new JRadioButton("Fastest");
        mShortestRadioBtn = new JRadioButton("Shortest");
        mCustomTrafficRadioBtn = new JRadioButton("Consider current traffic");
        weightingButtonGroup.add(mFastestRadioBtn);
        weightingButtonGroup.add(mShortestRadioBtn);
        weightingButtonGroup.add(mCustomTrafficRadioBtn);
        JPanel weightingPanel = new JPanel();
        weightingPanel.setBorder(BorderFactory.createTitledBorder("Route options"));
        weightingPanel.setOpaque(true);
        weightingPanel.setLayout(new GridLayout(3, 1));
        weightingPanel.add(mFastestRadioBtn);
        weightingPanel.add(mShortestRadioBtn);
        weightingPanel.add(mCustomTrafficRadioBtn);

        //frame.getContentPane().add(label);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setSize(300, 400);
        frame.setVisible(true);
        frame.setLayout(new GridLayout(5, 1));

        frame.add(inputDataPanel);
        frame.add(infoPanel);
        frame.add(buttonPanel);
        frame.add(calculateBtnPanel);
        frame.add(weightingPanel);

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

                    java.util.List<PositionWithTimeData> route = reader.readDb(id, Consts.TRAFFIC_TABLE, "date");

                    if (route.size() > 0) {
                        Point2D.Double startPoint = new Point2D.Double(route.get(0).getLatitude(),
                                route.get(0).getLongitude());
                        Point2D.Double finishPoint = new Point2D.Double(route.get(route.size() - 1).getLatitude(),
                                route.get(route.size() - 1).getLongitude());

                        OptimalRoute optimalRoute = optimalRouteCoverageCalc
                                .findOptimalRoute(startPoint, finishPoint, Consts.FASTEST, hopper);
                        if (optimalRoute.getRoute() != null) {
//                            reader.saveOptimalRouteIntoDb(id, optimalRoute.getRoute());
//                            double pointsCoverage = optimalRouteCoverageCalc.calculateOptimalRouteCoverage(id, route,
//                                    optimalRoute.getRoute(), optimalRoute.getTime(), optimalRoute.getRouteLength());
//                            System.out.println("Route #" + id + " - pointsCoverage: " + pointsCoverage);
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
                String chosenWeighting = getChosenWeighting();
                if (Consts.FASTEST.equals(chosenWeighting)
                        || Consts.SHORTEST.equals(chosenWeighting)
                        || Consts.CURRENT_TRAFFIC.equals(chosenWeighting)) {
                    //calculateRoutes(hopper, chosenWeighting);

                } else {
                    JOptionPane.showMessageDialog(frame, "Proszę wybrać opcje trasy");
                }
            }
        });

        //comparing optimal routes time
//        DataReader reader = new DataReader();
//        OptimalRouteCoverageCalc optimalRouteCoverageCalc = new OptimalRouteCoverageCalc();
////        int[] testRoutes = {14, 1747, 2159, 14815, 14816, 16998};
//        int[] testRoutes = {4027, 5454, 6379, 8343, 8782, 16521};
//        for (int testRoute : testRoutes) {
//            System.out.println("Route id: " + testRoute);
//            java.util.List<Point2D.Double> route = reader.readDb(testRoute, Consts.OPTIMAL_ROUTES_WITH_TRAFFIC_TABLE,
//                    "point_order");
//            optimalRouteCoverageCalc.compareOptimalRoutesTime(route, Consts.FASTEST, hopper);
//        }
    }

    public Gui(MyGraphHopper hopper, String startDate, String endDate,boolean isTrafficConsidered,
               String resultsFileName) {
        calculateRoutes(hopper, Consts.FASTEST, startDate, endDate, isTrafficConsidered, resultsFileName);
    }

    private void calculateRoutes(MyGraphHopper hopper, String chosenWeighting, String startDate, String endDate,
                                 boolean isTrafficConsidered, String resultsFileName) {
        DataReader reader = new DataReader();
        OptimalRouteCoverageCalc optimalRouteCoverageCalc = new OptimalRouteCoverageCalc();

        String columnToGet = "id";
        java.util.List<Integer> selectedRoutes = reader.readSelectedTrafficId(startDate, endDate, columnToGet);

        for (int id : selectedRoutes) {

            java.util.List<PositionWithTimeData> route = reader.readDb(id, Consts.TRAFFIC_WITHOUT_PARKING_TABLE, "date");

            if (route.size() > 10) {
                Point2D.Double startPoint = new Point2D.Double(route.get(0).getLatitude(),
                        route.get(0).getLongitude());
                Point2D.Double finishPoint = new Point2D.Double(route.get(route.size() - 1).getLatitude(),
                        route.get(route.size() - 1).getLongitude());
                OptimalRoute optimalRoute = optimalRouteCoverageCalc
                        .findOptimalRoute(startPoint, finishPoint, chosenWeighting, hopper);
                if (optimalRoute != null && optimalRoute.getRoute() != null) {
                    if (Consts.SAVE_OPTIMAL_ROUTE_INTO_DB) {
                        reader.saveOptimalRouteIntoDb(id, optimalRoute.getRoute(), isTrafficConsidered);
                    }
                    System.out.println("Route #" + id);
//                    double pointsCoverage = optimalRouteCoverageCalc
//                            .calculateOptimalRouteCoverage(id, route, optimalRoute.getRoute(),
//                                    optimalRoute.getTime(), optimalRoute.getRouteLength(), resultsFileName);
//                    System.out.println("PointsCoverage: " + pointsCoverage);
                    final String txtFileFormat = ".txt";
                    String bridgesOnRealRouteResultsFileName = resultsFileName.replace(txtFileFormat,
                            Consts.BRIDGES_REAL_SUFFIX + txtFileFormat);
                    String bridgesOnOptimalRouteResultsFileName;
                    if (isTrafficConsidered) {
                        bridgesOnOptimalRouteResultsFileName = resultsFileName.replace(txtFileFormat,
                                Consts.BRIDGES_CONSIDER_TRAFFIC_SUFFIX + txtFileFormat);
                    } else {
                        bridgesOnOptimalRouteResultsFileName = resultsFileName.replace(txtFileFormat,
                                Consts.BRIDGES_BLIND_SUFFIX + txtFileFormat);
                    }
                    optimalRouteCoverageCalc.findBridgesOnTheRoute(id, route, optimalRoute.getRoute(), hopper,
                            bridgesOnRealRouteResultsFileName, bridgesOnOptimalRouteResultsFileName, isTrafficConsidered);
                }
            }
        }
    }

    private String getChosenWeighting() {
        String chosenWeighting = null;
        if (mFastestRadioBtn.isSelected()) {
            chosenWeighting = Consts.FASTEST;
        } else if (mShortestRadioBtn.isSelected()) {
            chosenWeighting = Consts.SHORTEST;
        } else if (mCustomTrafficRadioBtn.isSelected()) {
            chosenWeighting = Consts.CURRENT_TRAFFIC;
        }
        return chosenWeighting;
    }

}
