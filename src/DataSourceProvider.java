import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * Created by lukasz on 10.01.18.
 */
public class DataSourceProvider {

    private static DataSource dataSource;

    private DataSourceProvider() {}

    public static DataSource provideDataSource() throws NamingException {
        if (dataSource == null) {
            InitialContext ctx = new InitialContext();
            dataSource = (DataSource) ctx.lookup(Consts.DATA_SOURCE_NAME);
        }
        return dataSource;
    }

}
