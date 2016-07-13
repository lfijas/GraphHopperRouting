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

    private static MyGraphHopper mHopperInstance;

    public static MyGraphHopper getHopperInstance() {
        return mHopperInstance;
    }

    public static void main(String[] args) {

        System.out.println("Program started");

        MyGraphHopper hopper = new MyGraphHopper();
        hopper.setOSMFile(OSM_FILE_PATH);

        hopper.setGraphHopperLocation(GRAPH_FOLDER);
        //hopper.clean();
        //hopper.setEncodingManager(new EncodingManager("car"));
        hopper.setEncodingManager(new CustomEncodingManager(CustomEncodingManager.CUSTOM_CAR));
        hopper.setCHEnable(false);
        hopper.importOrLoad();
        if (Consts.CONSIDER_TRAFFIC_FLAG || Consts.STORE_EXTRA_CUSTOM_SPEED) {
            hopper.loadTrafficData();
        }

        new Gui(hopper);

    }

}
