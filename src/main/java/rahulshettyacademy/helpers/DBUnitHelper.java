package rahulshettyacademy.helpers;

import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.ext.oracle.Oracle10DataTypeFactory;
import org.dbunit.operation.DatabaseOperation;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
public class DBUnitHelper {

    public static final String ORACLE_DRIVER = "oracle.jdbc.driver.OracleDriver";
    private static IDataSet loadedDataSet;

    public DBUnitHelper() {
    }

    /**
     * Method that Provides a connection to the database
     *
     * @return database connection instance
     */
    protected static IDatabaseConnection getConnection(final String connUrl,
                                                       final String dbUser,
                                                       final String dbPassword) throws Exception {

        Class.forName(ORACLE_DRIVER);

        Connection jdbcConnection = DriverManager.getConnection(connUrl, dbUser, dbPassword);

        return new DatabaseConnection(jdbcConnection, dbUser);
    }


    /**
     * Method that Loads the data which will be inserted for the test
     *
     * @return loaded xml data set
     */
    protected static IDataSet getDataSet(final String xml) throws Exception {
        FlatXmlDataSetBuilder flatXmlDataSetBuilder = new FlatXmlDataSetBuilder();
        flatXmlDataSetBuilder.setColumnSensing(true);
        loadedDataSet = flatXmlDataSetBuilder.build(new File(xml));
        return loadedDataSet;
    }

    /**
     * Method that disables the foreign key constraint in target DB
     */
    public static void disableAllContraints(final String connUrl,
                                            final String dbUser,
                                            final String dbPassword) throws Exception {

        DBHelper.initializeDatasource(connUrl, dbUser, dbPassword);
        DBHelper.disableAllContraints();
    }

    /**
     * Method that enables the foreign key constraint in target DB
     */
    public static void enableAllContraints(final String connUrl,
                                           final String dbUser,
                                           final String dbPassword) throws Exception {

        DBHelper.initializeDatasource(connUrl, dbUser, dbPassword);
        DBHelper.enableAllContraints();
    }

    /**
     * Method that disables the foreign key constraint in target DB
     */
    public static void resetAllSequences(final String connUrl,
                                         final String dbUser,
                                         final String dbPassword) throws Exception {

        resetAllSequences(connUrl, dbUser, dbPassword, 0);
    }

    /**
     * Method that disables the foreign key constraint in target DB
     */
    public static void resetAllSequences(final String connUrl,
                                         final String dbUser,
                                         final String dbPassword,
                                         final long initialSequenceNumber,
                                         final String... excludeSequences) throws Exception {

        DBHelper.initializeDatasource(connUrl, dbUser, dbPassword);
        DBHelper.resetAllSequences(initialSequenceNumber, excludeSequences);
    }

    /**
     * Method that deletes and then populates the DB
     */
    public static void initDB(final String connUrl,
                              final String dbUser,
                              final String dbPassword,
                              final String dbunitXml,
                              final long initialSequenceNumber,
                              final String... excludeSequences) throws Exception {
        initDB(connUrl, dbUser, dbPassword, dbunitXml, true, true, initialSequenceNumber, excludeSequences);
    }

    /**
     * Method that deletes and then populates the DB
     */
    public static void initDB(final String connUrl,
                              final String dbUser,
                              final String dbPassword,
                              final String dbunitXml,
                              final boolean useQualifiedNameTablesInDbUnitXml,
                              final long initialSequenceNumber,
                              final String... excludeSequences) throws Exception {
        initDB(connUrl, dbUser, dbPassword, dbunitXml, useQualifiedNameTablesInDbUnitXml, true, initialSequenceNumber, excludeSequences);
    }
    /**
     * Method that deletes and then populates the DB
     */
    public static void initDB(final String connUrl,
                              final String dbUser,
                              final String dbPassword,
                              final String dbunitXml,
                              final boolean useQualifiedNameTablesInDbUnitXml,
                              final boolean disableEnableConstraints,
                              final long initialSequenceNumber,
                              final String... excludeSequences) throws Exception {

        // disable referential constraints in the database
        if(disableEnableConstraints) {
            disableAllContraints(connUrl, dbUser, dbPassword);
        }

        //reset all the sequences.
        resetAllSequences(connUrl, dbUser, dbPassword, initialSequenceNumber, excludeSequences);

        // initialize database connection
        IDatabaseConnection connection = getConnection(connUrl, dbUser, dbPassword);
        DatabaseConfig config = connection.getConfig();
        config.setProperty(DatabaseConfig.FEATURE_QUALIFIED_TABLE_NAMES,
                useQualifiedNameTablesInDbUnitXml);
        config.setProperty(DatabaseConfig.PROPERTY_ESCAPE_PATTERN, "\"?\"");
        config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
        // initialize dataset
        IDataSet dataSet = getDataSet(dbunitXml);

        try {
            DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);
        } finally {
            connection.close();
        }
        if(disableEnableConstraints) {
            enableAllContraints(connUrl, dbUser, dbPassword);
        }
    }

    /**
     * Method that populates the DB using xml file
     */
    public static void populateDB(final String connUrl,
                                  final String dbUser,
                                  final String dbPassword,
                                  final String xml) throws Exception {
        populateDB(connUrl, dbUser, dbPassword, xml, true);
    }

    /**
     * Method that populates the DB using xml file
     */
    public static void populateDB(final String connUrl,
                                  final String dbUser,
                                  final String dbPassword,
                                  final String xml,
                                  final boolean useQualifiedNameTablesInDbUnitXml) throws Exception {

        // initialize database connection
        IDatabaseConnection connection = getConnection(connUrl, dbUser, dbPassword);
        DatabaseConfig config = connection.getConfig();
        config.setProperty(DatabaseConfig.FEATURE_QUALIFIED_TABLE_NAMES, useQualifiedNameTablesInDbUnitXml);
        config.setProperty(DatabaseConfig.PROPERTY_ESCAPE_PATTERN, "\"?\"");
        config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
        // initialize dataset
        IDataSet dataSet = getDataSet(xml);

        try {
            DatabaseOperation.INSERT.execute(connection, dataSet);
        } finally {
            connection.close();
        }

    }


}
