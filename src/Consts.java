/**
 * Created by lukasz on 18.01.16.
 */
public class Consts {

    public static final String TRAFFIC_TABLE = "Traffic";
    public static final String TRAFFIC_WITHOUT_PARKING_TABLE = "Traffic_without_parking";
    public static final String TRAFFIC_WITH_SPEED_TABLE= "Traffic_with_speed";
    public static final String OPTIMAL_COVERAGE_RESULTS_FILE
            = "/home/lukasz/Pulpit/Results_with_speed2/results_default_speed_19000_wo_parking_13_0815_0830.txt";

    public static final String SHORTEST = "shortest";
    public static final String FASTEST = "fastest";
    public static final String CURRENT_TRAFFIC = "current_traffic";

    //SQL queries
    public static final String MORNING_TRAFFIC_QUERY = "select id " +
            "from " +
            "(" +
            "SELECT min(date) as start_date, id FROM `Traffic_without_parking` group by id" +
            ") as a " +
            "where a.start_date between '2014-02-13 08:15:00' AND '2014-02-13 08:30:00'";

}
