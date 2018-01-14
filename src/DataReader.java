import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint3D;

import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

            DataSource ds = DataSourceProvider.provideDataSource();
            conn = ds.getConnection();

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
        } catch (NamingException e) {
            e.printStackTrace();
        } finally {
            close();
        }
        return resultPointsList;

    }

    public List<FullTrafficData> readFullTrafficData(String startDate, String endDate) {
        List<FullTrafficData> resultList = new ArrayList<FullTrafficData>();
        try {
            Class.forName("com.mysql.jdbc.Driver");

            DataSource ds = DataSourceProvider.provideDataSource();
            conn = ds.getConnection();

            String sqlQuery = "SELECT * " +
                    "FROM Traffic_with_speed " +
                    "WHERE Date BETWEEN ? AND ? " +
                    "AND Tag = 'WAW'";

            preparedStatement = conn.prepareStatement(sqlQuery);
            preparedStatement.setString(1, startDate);
            preparedStatement.setString(2, endDate);
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
        } catch (NamingException e) {
            e.printStackTrace();
        } finally {
            close();
        }
        return resultList;
    }

    public List<Integer> readSelectedTrafficId(String startDate, String endDate, String columnName) {
        List<Integer> resultList = new ArrayList<>();
        try {
            Class.forName("com.mysql.jdbc.Driver");

            DataSource ds = DataSourceProvider.provideDataSource();
            conn = ds.getConnection();

            String sqlQuery = "SELECT a.id " +
                    "FROM " +
                    "(" +
                    "SELECT id, min(date) as start_date " +
                    "FROM `Traffic_without_parking` " +
                    "WHERE tag = 'WAW' OR tag = 'WWW' " +
                    "group by id" +
                    ") as a " +
                    "where a.start_date between ? AND ?";

            preparedStatement = conn.prepareStatement(sqlQuery);
            preparedStatement.setString(1, startDate);
            preparedStatement.setString(2, endDate);
            resultSet = preparedStatement.executeQuery();

            while ((resultSet.next())) {
                resultList.add(resultSet.getInt(columnName));
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NamingException e) {
            e.printStackTrace();
        } finally {
            close();
        }
        return resultList;
    }

    public void saveOptimalRouteIntoDb(int routeId, PointList optimalRoute, boolean isTrafficConsidered) {

        deleteOptimalRouteFromDb(routeId, isTrafficConsidered);

        try {
            Class.forName("com.mysql.jdbc.Driver");

            DataSource ds = DataSourceProvider.provideDataSource();
            conn = ds.getConnection();

            String tableName = Consts.OPTIMAL_ROUTES_TABLE;
            if (isTrafficConsidered) {
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
        } catch (NamingException e) {
            e.printStackTrace();
        } finally {
            close();
        }
    }

    private void deleteOptimalRouteFromDb(int routeId, boolean isTrafficConsidered) {
        try {
            Class.forName("com.mysql.jdbc.Driver");

            DataSource ds = DataSourceProvider.provideDataSource();
            conn = ds.getConnection();

            String tableName = Consts.OPTIMAL_ROUTES_TABLE;
            if (isTrafficConsidered) {
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
        } catch (NamingException e) {
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
