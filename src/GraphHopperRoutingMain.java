/**
 * Created by lukasz on 13.10.15.
 */
public class GraphHopperRoutingMain {

    //POZ
    private static final String OSM_FILE_PATH_POZ
            = "/home/lukasz/Pulpit/Magisterskie/Praca_magisterska/GraphHopper/wielkopolskie-latest.osm.pbf";

    //WAW
    private static final String OSM_FILE_PATH_WAW
            = "/home/lukasz/Pulpit/Magisterskie/Praca_magisterska/GraphHopper/mazowieckie-latest.osm.pbf";

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
        //parsing arguments
        String startDate = args[0];
        String endDate = args[1];
        boolean isTrafficConsidered = Boolean.parseBoolean(args[2]);
        String resultsFileName = args[3];


        MyGraphHopper hopper = new MyGraphHopper();
        hopper.setOSMFile(OSM_FILE_PATH_WAW);

        hopper.setGraphHopperLocation(GRAPH_FOLDER);
        //hopper.clean();
        //hopper.setEncodingManager(new EncodingManager("car"));
        hopper.setEncodingManager(new CustomEncodingManager(CustomEncodingManager.CUSTOM_CAR));
        hopper.setCHEnable(false);
        hopper.importOrLoad();
        if (isTrafficConsidered || Consts.STORE_EXTRA_CUSTOM_SPEED) {
            hopper.loadTrafficData(startDate, endDate, isTrafficConsidered);
        }

        new Gui(hopper, startDate, endDate, isTrafficConsidered, resultsFileName);

    }

}
