/**
 * Copyright (C) 2005-2016, Stefan Strömberg <stefangs@nethome.nu>
 *
 * This file is part of OpenNetHome  (http://www.nethome.nu)
 *
 * OpenNetHome is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenNetHome is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package nu.nethome.home.item;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.h2.jdbc.JdbcSQLException;
import org.h2.jdbcx.JdbcConnectionPool;

/**
 * This is an H2 database implementation of the ValueItemLogger.
 *
 * @author Peter Lagerhem, 2015-12-30
 */
public class ValueItemLoggerH2Database extends ValueItemLogger {

    private enum STORE_ERROR {

        JDBC_EXCEPTION, MISSING_TABLE, NONE
    }

    private static Logger logger = Logger.getLogger(ValueItemLoggerH2Database.class.getName());
    private static final SimpleDateFormat DATEFORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private static final String DB_DRIVER = "org.h2.Driver";
    private static final String DB_PASSWORD = "sa";
    private static final String DB_USER = "sa";
    public static String UNIQUE_IDENTIFIER = "jdbc:h2";
    private final boolean autoCreateTables = true;

    /**
     * Create H2 table needed for the operation of this component.
     */
    private void createTable(String connectionString) {
        JdbcConnectionPool jdbcConnectionPool = getConnectionPool(connectionString);
        Statement createStatement;

        try (Connection connection = jdbcConnectionPool.getConnection()) {
            connection.setAutoCommit(false);

            createStatement = connection.createStatement();
            createStatement.execute(
                    "CREATE TABLE VALUELOGGER(valueItemId long not null, lastupdate timestamp not null, value varchar(255) not null)");
            createStatement.execute("ALTER TABLE VALUELOGGER ADD PRIMARY KEY (valueItemId, lastupdate)");

            createStatement.close();

            connection.commit();

            Logger.getLogger(ValueItemLoggerH2Database.class.getName()).log(Level.INFO,
                    "VALUELOGGER table has been created.");

        } catch (Exception e) {
            Logger.getLogger(ValueItemLoggerH2Database.class.getName()).log(Level.WARNING, null, e);
        }
    }

    /**
     * Create H2 JdbcConnectionPool
     *
     * @return JdbcConnectionPool
     */
    private JdbcConnectionPool getConnectionPool(String connectionString) {
        JdbcConnectionPool cp;
        try {
            Class.forName(DB_DRIVER);
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }
        cp = JdbcConnectionPool.create(connectionString, DB_USER, DB_PASSWORD);
        return cp;
    }

    @Override
    public List<Object[]> loadBetweenDates(String connectionString, String itemId, Date from, Date to) {
        List<Object[]> result = new ArrayList<>();

        JdbcConnectionPool jdbcConnectionPool = getConnectionPool(connectionString);
        PreparedStatement selectPreparedStatement;

        String SelectQuery = "SELECT * FROM VALUELOGGER WHERE lastupdate >= ? AND lastupdate <= ? AND valueItemId = ? ORDER BY valueitemid, lastupdate";
        try (Connection connection = jdbcConnectionPool.getConnection()) {
            selectPreparedStatement = connection.prepareStatement(SelectQuery);
            selectPreparedStatement.setTimestamp(1, new java.sql.Timestamp(from.getTime()));
            selectPreparedStatement.setTimestamp(2, new java.sql.Timestamp(to.getTime()));
            selectPreparedStatement.setString(3, itemId);

            ResultSet rs = selectPreparedStatement.executeQuery();
            while (rs.next()) {
                Double value = Double.valueOf(rs.getString("value"));
                Object[] row = { DATEFORMAT.format(rs.getTimestamp("lastupdate")), value };
                result.add(row);
            }
            selectPreparedStatement.close();
        } catch (JdbcSQLException e) {
            if (e.getOriginalMessage().compareToIgnoreCase("Table \"VALUELOGGER\" not found") == 0) {
                Logger.getLogger(ValueItemLoggerH2Database.class.getName()).log(Level.INFO, "Table is missing", e);
                if (autoCreateTables) {
                    createTable(connectionString);
                }
            }
        } catch (Exception e) {
            Logger.getLogger(ValueItemLoggerH2Database.class.getName()).log(Level.WARNING, null, e);
        } finally {
            jdbcConnectionPool.dispose();
        }
        return result;
    }

    @Override
    public boolean store(String connectionString, String itemId, String value) {
        Date justNow = new Date();
        return storeWithDate(connectionString, itemId, value, justNow);
    }

    public boolean storeWithDate(String connectionString, String itemId, String value, Date date) {
        STORE_ERROR result = tryStore(connectionString, itemId, value, date);
        if (result == STORE_ERROR.MISSING_TABLE) {
            if (autoCreateTables) {
                createTable(connectionString);
                result = tryStore(connectionString, itemId, value, date);
            }
        }
        if (result == STORE_ERROR.NONE) {
            return true;
        }
        Logger.getLogger(ValueItemLoggerH2Database.class.getName()).log(Level.WARNING,
                "Can't store value to H2 database. (" + result.name() + ")");
        return false;
    }

    /**
     * Will try store the value into the H2 database. Upon any thrown exception,
     * a check is made if the database is not yet created and return
     * MISSING_TABLE error.
     *
     * @param connectionString
     *            connectionString
     * @param itemId
     *            a unique id associated with values stored to the destination
     * @param value
     *            the value to try to store into the H2 database.
     * @return Any of the STORE_ERROR internal error codes.
     */
    private STORE_ERROR tryStore(String connectionString, String itemId, String value, Date aDate) {

        STORE_ERROR result = STORE_ERROR.NONE;

        JdbcConnectionPool jdbcConnectionPool = getConnectionPool(connectionString);
        PreparedStatement preparedStatement;
        value = value.replace(',', '.');

        String Query = "INSERT INTO VALUELOGGER(valueItemId, lastupdate, value) values" + "(?,?,?)";
        try (Connection connection = jdbcConnectionPool.getConnection()) {
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(Query);

            preparedStatement.setString(1, itemId);
            preparedStatement.setTimestamp(2, new java.sql.Timestamp(aDate.getTime()));
            preparedStatement.setString(3, value);
            preparedStatement.executeUpdate();
            preparedStatement.close();
            connection.commit();
        } catch (JdbcSQLException e) {
            if (e.getOriginalMessage().compareToIgnoreCase("Table \"VALUELOGGER\" not found") == 0) {
                Logger.getLogger(ValueItemLoggerH2Database.class.getName()).log(Level.INFO, "Table is missing", e);
                result = STORE_ERROR.MISSING_TABLE;
            }
        } catch (Exception e) {
            Logger.getLogger(ValueItemLoggerH2Database.class.getName()).log(Level.WARNING, null, e);
            result = STORE_ERROR.JDBC_EXCEPTION;
        } finally {
            jdbcConnectionPool.dispose();
        }
        return result;
    }

    /**
     * Imports the provided CSV file into the database. Will make sure not
     * import already imported values for the home item.
     */
    public boolean importCsvFile(String csvFileName, String connectionString, String itemId) {

        SimpleDateFormat fileDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        BufferedReader br;
        int importCount = 0;
        int lineCount = 0;
        int linesInBatch = 0;
        boolean success = true;

        try {
            // Open the data file
            FileReader reader = new FileReader(csvFileName);
            br = new BufferedReader(reader);
            String line;

            // Connect to db
            JdbcConnectionPool jdbcConnectionPool = getConnectionPool(connectionString);
            Connection connection = jdbcConnectionPool.getConnection();
            PreparedStatement preparedStatement;
            String Query = "INSERT INTO VALUELOGGER (VALUEITEMID, LASTUPDATE, VALUE) SELECT ?,?,? WHERE NOT EXISTS (SELECT 1 FROM VALUELOGGER WHERE LASTUPDATE = ? and VALUEITEMID = ?)";
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(Query);
            preparedStatement.setString(1, itemId);
            preparedStatement.setString(5, itemId);

            final int batchSize = 500;

            logger.log(Level.INFO, "Begin import of data from file: " + csvFileName);

            try {
                while ((line = br.readLine()) != null) {
                    success = true;
                    try {
                        lineCount++;

                        // Get next log entry
                        if (line.length() > 21) {
                            // Adapt the time format
                            String minuteTime = line.substring(0, 16).replace('.', '-');
                            // Parse the time stamp
                            Date min = fileDateFormat.parse(minuteTime);

                            // Check if value is within time window
                            double value = Double.parseDouble((line.substring(20)).replace(',', '.'));
                            String valueS = String.valueOf(value);

                            java.sql.Timestamp sqlDate = new java.sql.Timestamp(min.getTime());

                            preparedStatement.setTimestamp(2, sqlDate);
                            preparedStatement.setString(3, valueS);
                            preparedStatement.setTimestamp(4, sqlDate);
                            preparedStatement.addBatch();
                            linesInBatch++;

                            if (lineCount % batchSize == 0) {
                                preparedStatement.executeBatch();
                                importCount += preparedStatement.getUpdateCount() * batchSize;
                                linesInBatch = 0;
                            }

                        }
                    } catch (NumberFormatException nfe) {
                        // Bad number format in a line, ignore and try to
                        // continue
                    } catch (JdbcSQLException e) {
                        if (e.getOriginalMessage().compareToIgnoreCase("Table \"VALUELOGGER\" not found") == 0) {
                            Logger.getLogger(ValueItemLoggerH2Database.class.getName()).log(Level.INFO,
                                    "Table is missing", e);
                            System.out.println("Can't continue...");
                            // Could create the table automatically, but since
                            // the view graph does this
                            // for us anyways, we will skip it here.
                            // if (autoCreateTables) { createTable(); }
                        }
                        break;
                    } catch (SQLException ex) {
                        Logger.getLogger(ValueItemLoggerH2Database.class.getName()).log(Level.WARNING, null, ex);
                        System.out.println("Can't continue...");
                        break;
                    } catch (Exception e) {
                        Logger.getLogger(ValueItemLoggerH2Database.class.getName()).log(Level.WARNING,
                                "Connecting with: " + connectionString, e);
                        System.out.println("Can't continue...");
                        break;
                    }
                }
            } catch (Exception e) {
                System.out.println(e.toString());

            } finally {
                try {
                    preparedStatement.executeBatch(); // insert remaining
                                                      // records
                    importCount += linesInBatch;

                    br.close();
                    preparedStatement.close();
                    connection.commit();
                    connection.close();
                    jdbcConnectionPool.dispose();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (FileNotFoundException f) {
            System.out.println(f.toString());
            logger.log(Level.INFO, f.toString());
        } catch (SQLException e1) {
            e1.printStackTrace();
        }

        if (success) {
            if (importCount > 0) {
                logger.log(Level.INFO, "Successfully imported " + importCount + " log entries for item id: " + itemId
                        + " containing " + lineCount + " rows.");
            } else {
                logger.log(Level.INFO,
                        "No entries were imported for item id: " + itemId + " containing " + lineCount + " rows.");
            }
        }

        return true;
    }

}
