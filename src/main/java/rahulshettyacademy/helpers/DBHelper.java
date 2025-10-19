package rahulshettyacademy.helpers;

import oracle.jdbc.pool.OracleDataSource;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.jooq.lambda.Unchecked;
import java.io.InputStream;
import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class DBHelper {

    private static final Logger LOG = Logger.getLogger(DBHelper.class.getName());
    private static OracleDataSource dataSource = null;

    private static final String DBMS_GET_OUTPUT = "DECLARE "
            + " num INTEGER := 1000;"
            + "BEGIN "
            + " dbms_output.get_lines(?, num);"
            + "END;";
    private static final String EXECUTE_IMIDEATELY = "begin \n" +
            "execute immediate q'{ %s }'; \n" +
            "end;";

    private static final String PLSQL_BLOCK_CONSTRAINTS = "begin \n"
            + " for rec in (select c.table_name, c.constraint_name, c.constraint_type from user_constraints c, user_tables u where c.table_name=u.table_name and c.constraint_type = 'R' and c.status = '%s') \n"
            + " loop \n"
            + " dbms_output.put_line('%S constraint ' || rec.constraint_name || ' on table ' || rec.table_name); \n"
            + " begin\n"
            + " execute immediate 'alter table ' || rec.table_name || ' %s constraint ' || rec.constraint_name; \n"
            + " exception\n"
            + " when others then\n"
            + " dbms_output.put_line('An error was encountered - ' || SQLCODE || ' -ERROR- ' || SQLERRM); \n"
            + " end;\n"
            + " end loop; \n"
            + "end; ";


    private static final String SEQ_PREDICATE = "WHERE sequence_name = \'%s\'";
    private static final String SEQ_NOT_PREDICATE = "and sequence_name not in (%s)";
    private static final String SEQ_MIN_VALUE = "rec.min_value;";
    private static final String RESET_ALL_SEQUENCES_SQL = "declare\n" +
            " l_val integer;\n" +
            " min_val integer;\n" +
            " incrementBy integer;\n" +
            "begin \n" +
            " for rec in (select sequence_name, min_value, increment_by from user_sequences %s ) \n" +
            " loop \n" +
            " -- just reset\n" +
            " incrementBy := rec.increment_by; \n" +
            " if incrementBy <> 1 Then \n" +
            " incrementBy := 1; \n" +
            " execute immediate 'alter sequence ' || rec.sequence_name || ' increment by 1 minvalue ' || rec.min_value; \n" +
            " end if; \n" +
            " execute immediate 'select ' || rec.sequence_name || '.nextval from dual' INTO l_val;\n" +
            " min_val := %s;\n" +
            " if l_val > min_val then\n" +
            " l_val := l_val - min_val; \n" +
            " dbms_output.put_line('reset sequence ' || rec.sequence_name || ', target_value ' || min_val || ', min_val ' || rec.min_value\n" +
            " || ', increment_by ' || incrementBy ||', current_value: ' || l_val);\n" +
            " execute immediate 'alter sequence ' || rec.sequence_name || ' increment by -' || l_val || ' minvalue ' || rec.min_value;\n" +
            " execute immediate 'select ' || rec.sequence_name || '.nextval from dual' INTO l_val; \n" +
            " execute immediate 'alter sequence ' || rec.sequence_name || ' increment by ' || incrementBy || ' minvalue ' || rec.min_value;\n" +
            " execute immediate 'select ' || rec.sequence_name || '.currval from dual' INTO l_val;\n" +
            " dbms_output.put_line('>>> ' || l_val);\n" +
            " elsif l_val < min_val then\n" +
            " dbms_output.put_line('increasing sequence ' || rec.sequence_name || ', target_value ' || min_val || ', min_value ' || rec.min_value \n" +
            " || ', increment_by ' || incrementBy ||', current_value: ' || l_val);\n" +
            " l_val := min_val - l_val; \n" +
            " execute immediate 'alter sequence ' || rec.sequence_name || ' increment by ' || l_val || ' minvalue ' || rec.min_value;\n" +
            " execute immediate 'select ' || rec.sequence_name || '.nextval from dual' INTO l_val; \n" +
            " execute immediate 'alter sequence ' || rec.sequence_name || ' increment by ' || incrementBy || ' minvalue ' || rec.min_value;\n" +
            " execute immediate 'select ' || rec.sequence_name || '.currval from dual' INTO l_val;\n" +
            " dbms_output.put_line('>>> ' || l_val);\n" +
            " end if; \n" +
            " end loop; \n" +
            "end;";



    /**
     * enable all constraints from current schema.
     */
    protected static final String ENABLE_CONSTRAINTS_SQL = String.format(PLSQL_BLOCK_CONSTRAINTS, "DISABLED", "Enable", "ENABLE");

    /**
     * disable all constraints from current schema.
     */
    protected static final String DISABLE_CONSTRAINTS_SQL = String.format(PLSQL_BLOCK_CONSTRAINTS, "ENABLED", "Disable", "DISABLE");



    public static synchronized OracleDataSource initializeDatasource(final String connectionUrl, final String user, final String password) {
        try {
            if (dataSource == null) {
                dataSource = new OracleDataSource();
                dataSource.setConnectionCacheName("mycache");
                dataSource.setURL(connectionUrl);
                dataSource.setUser(user);
                dataSource.setPassword(password);

                Properties cacheProps = new Properties();
                cacheProps.setProperty("MinLimit", "1");
                cacheProps.setProperty("MaxLimit", "4");
                cacheProps.setProperty("InitialLimit", "1");
                cacheProps.setProperty("ConnectionWaitTimeout", "5");
                cacheProps.setProperty("ValidateConnection", "true");

                dataSource.setConnectionProperties(cacheProps);
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Error creatig connection : " + connectionUrl, e);
        }
        return dataSource;
    }

    /**
     * dispose the datasource.
     */
    public static synchronized void shutdownDatasource() {
        try {
            if (dataSource != null) {
                dataSource.close();
                dataSource = null;
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Error closing datasource ", e);
        }
    }

    /**
     * Gets the dataSource.
     * DbHelper.createDatasource should be called in a @BeforeClass
     * to initialize the pool.
     *
     * @return value of dataSource
     */
    private static OracleDataSource getDataSource() {
        Assert.assertNotNull(dataSource);
        return dataSource;
    }


    /**
     * execute a ddl statement.
     *
     * @param ddlStatement
     * @return
     */
    public static boolean executeDDL(final String ddlStatement) throws SQLException {
        assert ddlStatement != null && !ddlStatement.isEmpty();
        String statement = ddlStatement.trim();
        //minor protection for ending statement.
        if (statement.endsWith(";")) {
            statement = statement.substring(0, statement.lastIndexOf(";"));
        }
        //do not allow any other ";"
        assert !statement.contains(";");
        String plsql = String.format(EXECUTE_IMIDEATELY, statement);
        return executeStatment(plsql);
    }

    /**
     * disable all contraints from user schema
     *
     * @return
     */
    public static boolean disableAllContraints() throws SQLException {
        return executeStatmentWithOutput(DISABLE_CONSTRAINTS_SQL);
    }

    /**
     * enable all constraints from user schema.
     * <p>
     * return
     */
    public static boolean enableAllContraints() throws SQLException {
        executeStatmentWithOutput(ENABLE_CONSTRAINTS_SQL);
        //execute twice
        executeStatmentWithOutput(ENABLE_CONSTRAINTS_SQL);
        return true;
    }

    /**
     * enable all constraints from user schema.
     * <p>
     * return
     */
    public static boolean resetAllSequences(final long initialValue, String... excludeSequences) throws SQLException {
        if(initialValue <= 0){
            //skip
            return true;
        }
        String exclude = "WHERE sequence_name not like '%$%' ";
        if (excludeSequences.length > 0) {
            exclude += String.format(SEQ_NOT_PREDICATE,
                    Stream.of(excludeSequences)
                            .map(s -> '\'' + s + '\'')
                            .collect(Collectors.joining(", ")));
        }
        return executeStatmentWithOutput(
                String.format(RESET_ALL_SEQUENCES_SQL, exclude,
                        (initialValue > 0 ? String.valueOf(initialValue) : SEQ_MIN_VALUE)));
    }

    /**
     * enable all constraints from user schema.
     * <p>
     * return
     */
    public static void resetSequences(final long initialValue, String... sequenceNames) throws SQLException {
        if(initialValue <= 0){
            //skip
            return;
        }
        try {
            Stream.of(sequenceNames)
                    .map(seq -> String.format(SEQ_PREDICATE, seq))
                    .forEach(Unchecked.consumer(sequence -> executeStatmentWithOutput(
                            String.format(RESET_ALL_SEQUENCES_SQL,
                                    sequence,
                                    (initialValue > 0 ? String.valueOf(initialValue) : SEQ_MIN_VALUE)))));
        } catch (RuntimeException e) {
            if (e.getCause() instanceof SQLException) {
                throw (SQLException) e.getCause();
            } else {
                //just rethrow
                throw e;
            }
        }
    }


    /**
     * execute a sql statement.
     *
     * @param sql
     * @return
     */
    private static boolean executeStatment(final String sql) throws SQLException {
        assert dataSource != null;
        assert sql != null && sql.length() > 0;

        Connection databaseConnection = null;
        CallableStatement cs = null;
        try {
            databaseConnection = getDataSource().getConnection();
            LOG.info("Executing : \n" + sql);
            cs = databaseConnection.prepareCall(sql);
            return cs.execute();
        } finally {
            dispose(cs);
            dispose(databaseConnection);
        }
    }

    /**
     * execute a sql statement.
     *
     * @param sql
     * @return
     * @throws Exception
     */
    protected static JSONArray executeStatmentWithJSONResults(final String sql) throws Exception {
        assert dataSource != null;
        assert sql != null && sql.length() > 0;

        Connection databaseConnection = null;
        Statement cs = null;
        ResultSet rs;
        JSONArray resultsJSON;
        try {

            databaseConnection = getDataSource().getConnection();
            LOG.info("Executing : \n" + sql);
            cs = databaseConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY,
                    ResultSet.HOLD_CURSORS_OVER_COMMIT);

            rs = cs.executeQuery(sql);

            resultsJSON = convertToJSON(rs);

            return resultsJSON;
        } finally {
            dispose(cs);
            dispose(databaseConnection);
        }
    }

    /**
     * execute a sql statement.
     *
     * @param sql
     * @return
     * @throws Exception
     */
    protected static InputStream executeStatmentWithBinaryResult(final String sql, final String columnName) throws Exception {
        assert dataSource != null;
        assert sql != null && sql.length() > 0;

        Connection databaseConnection = null;
        Statement cs = null;
        ResultSet rs;
        InputStream is = null;
        try {

            databaseConnection = getDataSource().getConnection();
            LOG.info("Executing : \n" + sql);
            cs = databaseConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY,
                    ResultSet.HOLD_CURSORS_OVER_COMMIT);

            rs = cs.executeQuery(sql);

            while (rs.next()) {
                is = rs.getBinaryStream(columnName);
            }

            return is;

        } finally {
            dispose(cs);
            dispose(databaseConnection);
        }
    }

    /**
        * execute a sql statement.
            *
            * @param sql
 * @return
         */
    protected static boolean executeStatmentWithOutput(final String sql) throws SQLException {
        assert dataSource != null;
        assert sql != null && sql.length() > 0;

        Connection databaseConnection = null;
        Statement s = null;
        CallableStatement cs = null;
        try {
            databaseConnection = getDataSource().getConnection();
            s = databaseConnection.createStatement();
            s.executeUpdate("BEGIN dbms_output.enable(1000000); END;");
            LOG.info("Executing : \n" + sql);
            cs = databaseConnection.prepareCall(sql);
            return cs.execute();
        } finally {
            if (databaseConnection != null) {
                StringBuilder sb = getOutputLines(databaseConnection);
                LOG.info("script output is: \n" + sb);
                s.executeUpdate("BEGIN dbms_output.disable(); END;");
            }
            dispose(cs);
            dispose(databaseConnection);
        }
    }

    /**
     * get output...
     *
     * @param con
     * @return
     */
    private static StringBuilder getOutputLines(final Connection con) {
        StringBuilder sb = new StringBuilder();
        try {
            CallableStatement call = con.prepareCall(DBMS_GET_OUTPUT);
            call.registerOutParameter(1, Types.ARRAY, "DBMSOUTPUT_LINESARRAY");
            call.execute();
            Array array = null;
            try {
                array = call.getArray(1);
                Stream.of((Object[]) array.getArray())
                        .filter(Objects::nonNull)
                        .forEach(l -> sb.append(l).append("\n"));
            } finally {
                if (array != null) {
                    array.free();
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Error getting output: ", e);
        }
        return sb;
    }

    /**
     * close connection or statment or resultset.
     *
     * @param c
     */
    private static void dispose(final AutoCloseable c) {
        try {
            if (c != null) {
                c.close();
            }
        } catch (Exception e) {
            LOG.log(Level.FINEST, "Error closing " + c.getClass().getName(), e);
        }
    }

    /**
     * format the sql.
     *
     * @param sql
     * @return
     */
//    protected static String formatSql(final String sql) {
//   //     String currentSchema = ConfigurationHelper.INSTANCE.getProperty(ConfigurationHelper.DBUSERNAME);
//    //    String sourceSchema = ConfigurationHelper.INSTANCE.getProperty(ConfigurationHelper.DB_SCHEMA_REF);
//    //    return String.format(sql, currentSchema, sourceSchema);
//    }

    /**
     * convert result set to json
     *
     * @param resultSet
     * @return
     */
    public static JSONArray convertToJSON(ResultSet resultSet)
            throws Exception {
        JSONArray jsonArray = new JSONArray();
        while (resultSet.next()) {
            JSONObject obj = new JSONObject();
            int total_rows = resultSet.getMetaData().getColumnCount();
            String key = null;
            Object value = null;
            for (int i = 0; i < total_rows; i++) {

                key = resultSet.getMetaData().getColumnLabel(i + 1);
                value = resultSet.getObject(i + 1);

                obj.put(key, value);
            }

            jsonArray.put(obj);
        }
        return jsonArray;
    }


}
