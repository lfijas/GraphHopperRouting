import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint3D;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lukasz on 27.10.15.
 */
public class DataReader {

    private static final String connAddr = "jdbc:mysql://localhost/yanosik?user=root&password=admin";

    private Connection conn;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    public List<PositionWithTimeData> readDb(int id, String tableName, String orderByColumn) {
        List<PositionWithTimeData> resultPointsList = new ArrayList<PositionWithTimeData>();
        try {
            Class.forName("com.mysql.jdbc.Driver");

            conn = DriverManager.getConnection(connAddr);

            //statement = conn.createStatement();
            //resultSet = statement.executeQuery("select * from yanosik.Traffic where id = 2 order by Date");

            String sqlQuery = "select * from yanosik.%s"
                                        + " where id = ? order by %s";

            preparedStatement = conn.prepareStatement(String.format(sqlQuery, tableName, orderByColumn));
//            preparedStatement = conn.prepareStatement("select * from yanosik." + tableName
//                    + " where id = ?");
            preparedStatement.setInt(1, id);
            resultSet = preparedStatement.executeQuery();

            while ((resultSet.next())) {
                PositionWithTimeData positionWithTimeData = new PositionWithTimeData();
                positionWithTimeData.setLatitude(resultSet.getDouble("Latitude"));
                positionWithTimeData.setLongitude(resultSet.getDouble("Longitude"));
                positionWithTimeData.setTimestamp(resultSet.getTimestamp("Date"));
                resultPointsList.add(positionWithTimeData);
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close();
        }
        return resultPointsList;

    }

    public List<FullTrafficData> readFullTrafficData(int id, String tableName) {
        List<FullTrafficData> resultList = new ArrayList<FullTrafficData>();
        try {
            Class.forName("com.mysql.jdbc.Driver");

            conn = DriverManager.getConnection(connAddr);

            preparedStatement = conn.prepareStatement("select * from yanosik." + tableName
                    + " where id = ? order by Date");
            preparedStatement.setInt(1, id);
            resultSet = preparedStatement.executeQuery();

            while ((resultSet.next())) {
                FullTrafficData fullTrafficData = new FullTrafficData();
                fullTrafficData.setLatitude(resultSet.getDouble("Latitude"));
                fullTrafficData.setLongitude(resultSet.getDouble("Longitude"));
                fullTrafficData.setSpeed(resultSet.getInt("Speed"));
                fullTrafficData.setAzimuth(resultSet.getInt("Azimuth"));
                resultList.add(fullTrafficData);
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close();
        }
        return resultList;
    }

    public List<Integer> readSelectedTrafficId(String query) {
        List<Integer> resultList = new ArrayList<>();
        try {
            Class.forName("com.mysql.jdbc.Driver");

            conn = DriverManager.getConnection(connAddr);

            preparedStatement = conn.prepareStatement(query);
            resultSet = preparedStatement.executeQuery();

            while ((resultSet.next())) {
                resultList.add(resultSet.getInt("Id"));
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close();
        }
        return resultList;
    }

    public void saveOptimalRouteIntoDb(int routeId, PointList optimalRoute) {

        deleteOptimalRouteFromDb(routeId);

        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(connAddr);
            String tableName = Consts.OPTIMAL_ROUTES_TABLE;
            if (Consts.CONSIDER_TRAFFIC_FLAG) {
                tableName = Consts.OPTIMAL_ROUTES_WITH_TRAFFIC_TABLE;
            }
            String sql = "insert into yanosik." + tableName +
                " (id, latitude, longitude, point_order) values (?, ?, ?, ?)";
            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1, routeId);
            int order = 1;
            for (GHPoint3D point : optimalRoute) {
                preparedStatement.setDouble(2, point.getLat());
                preparedStatement.setDouble(3, point.getLon());
                preparedStatement.setInt(4, order);
                preparedStatement.executeUpdate();
                order++;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close();
        }
    }

    private void deleteOptimalRouteFromDb(int routeId) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(connAddr);
            String tableName = Consts.OPTIMAL_ROUTES_TABLE;
            if (Consts.CONSIDER_TRAFFIC_FLAG) {
                tableName = Consts.OPTIMAL_ROUTES_WITH_TRAFFIC_TABLE;
            }
            String sql = "delete from yanosik." + tableName + " where id = ?";
            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1, routeId);
            preparedStatement.executeUpdate();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close();
        }

    }

    private void close() {
        try {
            if (resultSet != null) {
                resultSet.close();
            }

            if (preparedStatement != null) {
                preparedStatement.close();
            }

            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
