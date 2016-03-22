import com.graphhopper.util.PointList;

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

    public Gui() {

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

                    java.util.List<Point2D.Double> route = reader.readDb(id, Consts.TRAFFIC_TABLE);

                    if (route.size() > 0) {
                        Point2D.Double startPoint = route.get(0);
                        Point2D.Double finishPoint = route.get(route.size() - 1);

                        PointList optimalRoute = optimalRouteCoverageCalc
                                .findOptimalRoute(startPoint, finishPoint, Consts.FASTEST);
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
                String chosenWeighting = getChosenWeighting();
                if (Consts.FASTEST.equals(chosenWeighting)
                        || Consts.SHORTEST.equals(chosenWeighting)
                        || Consts.CURRENT_TRAFFIC.equals(chosenWeighting)) {
                    DataReader reader = new DataReader();
                    OptimalRouteCoverageCalc optimalRouteCoverageCalc = new OptimalRouteCoverageCalc();

                    for (int id = 1; id < 100; id++) {

                        java.util.List<Point2D.Double> route = reader.readDb(id, Consts.TRAFFIC_TABLE);

                        if (route.size() > 10) {
                            Point2D.Double startPoint = route.get(0);
                            Point2D.Double finishPoint = route.get(route.size() - 1);
                            PointList optimalRoute = optimalRouteCoverageCalc
                                    .findOptimalRoute(startPoint, finishPoint, chosenWeighting);
                            if (optimalRoute != null) {
                                reader.saveOptimalRouteIntoDb(id, optimalRoute);
                                System.out.println("Route #" + id);
                                double pointsCoverage = optimalRouteCoverageCalc
                                        .calculateOptimalRouteCoverage(route, optimalRoute);
                                System.out.println("PointsCoverage: " + pointsCoverage);
                            }
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "Proszę wybrać opcje trasy");
                }
            }
        });
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
