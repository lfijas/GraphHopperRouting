import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.InitialContextFactoryBuilder;
import javax.naming.spi.NamingManager;
import javax.sql.DataSource;
import java.util.Hashtable;

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

        setupInitialContext();

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

    private static void setupInitialContext() {
        try {
            NamingManager.setInitialContextFactoryBuilder(new InitialContextFactoryBuilder() {
                @Override
                public InitialContextFactory createInitialContextFactory(Hashtable<?, ?> hashtable) throws NamingException {
                    return new InitialContextFactory() {
                        @Override
                        public Context getInitialContext(Hashtable<?, ?> hashtable) throws NamingException {
                            return new InitialContext() {
                                private Hashtable<String, DataSource> dataSources = new Hashtable<>();

                                @Override
                                public Object lookup(String name) throws NamingException {

                                    if (dataSources.isEmpty()) {
                                        MysqlConnectionPoolDataSource ds = new MysqlConnectionPoolDataSource();
                                        ds.setURL("jdbc:mysql://localhost/yanosik");
                                        ds.setUser("root");
                                        ds.setPassword("admin");
                                        dataSources.put(Consts.DATA_SOURCE_NAME, ds);
                                    }

                                    if (dataSources.containsKey(name)) {
                                        return dataSources.get(name);
                                    }

                                    throw new NamingException("Unable to find datasource: " + name);
                                }
                            };
                        }
                    };
                }
            });
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

}
