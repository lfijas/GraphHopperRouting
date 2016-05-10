import com.graphhopper.routing.util.EncodingManager;

/**
 * Created by lukasz on 13.10.15.
 */
public class GraphHopperRoutingMain {

    private static final String OSM_FILE_PATH
            = "/home/lukasz/Pulpit/Magisterskie/Praca_magisterska/GraphHopper/wielkopolskie-latest.osm.pbf";
    private static final String CH_GRAPH_FOLDER
            = "/home/lukasz/Pulpit/Magisterskie/Praca_magisterska/GraphHopper/Dev/GraphHopper_routing/generated_ch_graph";
    private static final String GRAPH_FOLDER
            = "/home/lukasz/Pulpit/Magisterskie/Praca_magisterska/GraphHopper/Dev/GraphHopper_routing/generated_graph";


    public static void main(String[] args) {

        System.out.println("Program started");

        MyGraphHopper hopper = new MyGraphHopper();
        hopper.setOSMFile(OSM_FILE_PATH);

        hopper.setGraphHopperLocation(GRAPH_FOLDER);
        hopper.setEncodingManager(new EncodingManager("car"));
        hopper.setCHEnable(false);
        hopper.importOrLoad();
        //hopper.loadTrafficData();

        new Gui(hopper);

    }

}
