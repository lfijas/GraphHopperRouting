/**
 * Created by lukasz on 18.01.16.
 */
public class Consts {

    public static final String TRAFFIC_TABLE = "Traffic";
    public static final String TRAFFIC_WITHOUT_PARKING_TABLE = "Traffic_without_parking";
    public static final String TRAFFIC_WITH_SPEED_TABLE= "Traffic_with_speed";
    public static final String OPTIMAL_ROUTES_TABLE = "Optimal_routes";
    public static final String OPTIMAL_ROUTES_WITH_TRAFFIC_TABLE = "Optimal_routes_with_traffic";

    public static final boolean CONSIDER_TRAFFIC_FLAG = true;
    public static final boolean STORE_EXTRA_CUSTOM_SPEED = true;

    public static final boolean SAVE_OPTIMAL_ROUTE_INTO_DB = false;

    public static final String OPTIMAL_COVERAGE_RESULTS_FILE
            = "/home/lukasz/Pulpit/Results_WAW_Bridges/results_custom_speed_custom_route_wo_parking_waw_19_1715_1730.txt";

    public static final String SHORTEST = "shortest";
    public static final String FASTEST = "fastest";
    public static final String CURRENT_TRAFFIC = "current_traffic";

    public static final String BRIDGES_REAL_SUFFIX = "_bridges_real";
    public static final String BRIDGES_BLIND_SUFFIX = "_bridges_blind";
    public static final String BRIDGES_CONSIDER_TRAFFIC_SUFFIX = "_bridges_traffic";

    //SQL queries
    public static final String ROUTES_TO_ANALYZE_QUERY = "SELECT a.id " +
            "FROM " +
            "(" +
            "SELECT id, min(date) as start_date " +
            "FROM `Traffic_without_parking` " +
            "WHERE tag = 'WAW' OR tag = 'WWW' " +
            "group by id" +
            ") as a " +
            "where a.start_date between '2015-02-19 17:15:00' AND '2015-02-19 17:30:00'";

    public static final String LOAD_TRAFFIC_QUERY = "SELECT * " +
            "FROM Traffic_with_speed " +
            "WHERE Date BETWEEN '2015-02-19 17:15:00' AND '2015-02-19 17:30:00' " +
            "AND Tag = 'WAW'";

}
