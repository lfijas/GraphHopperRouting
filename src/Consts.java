/**
 * Created by lukasz on 18.01.16.
 */
public class Consts {

    public static final String TRAFFIC_TABLE = "Traffic";
    public static final String TRAFFIC_WITHOUT_PARKING_TABLE = "Traffic_without_parking";
    public static final String TRAFFIC_WITH_SPEED_TABLE= "Traffic_with_speed";
    public static final String OPTIMAL_ROUTES_TABLE = "Optimal_routes";
    public static final String OPTIMAL_ROUTES_WITH_TRAFFIC_TABLE = "Optimal_routes_with_traffic";

    public static final boolean CONSIDER_TRAFFIC_FLAG = false;
    public static final boolean STORE_EXTRA_CUSTOM_SPEED = true;

    public static final boolean SAVE_OPTIMAL_ROUTE_INTO_DB = false;

    public static final String OPTIMAL_COVERAGE_RESULTS_FILE
            = "/home/lukasz/Pulpit/Results_WAW/results_custom_speed_default_route_wo_parking_waw_12_0800_0815.txt";

    public static final String SHORTEST = "shortest";
    public static final String FASTEST = "fastest";
    public static final String CURRENT_TRAFFIC = "current_traffic";

    //SQL queries
    public static final String MORNING_TRAFFIC_QUERY = "select a.id, a.original_id " +
            "from " +
            "(" +
            "SELECT id, original_id, min(date) as start_date,tag FROM `Traffic_without_parking` group by id" +
            ") as a " +
            "where a.start_date between '2015-02-12 08:00:00' AND '2015-02-12 08:15:00'" +
            " AND (a.tag = 'WAW' OR a.tag = 'WWW')";

}
