package solar.rpg.ticketer.data;

import solar.rpg.ticketer.controller.DataController;
import solar.rpg.ticketer.models.*;

import javax.swing.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Simple yet direct Database model that uses the JDBC driver.
 * Performs all actions that retrieve data from the tables.
 *
 * @author Joshua skinner
 * @version 1
 * @since 0.1
 */
public class Database {

    // Database fields.
    private final String user, pass, url, database;
    private Connection connection;

    // Reference to DataController for utility methods.
    private DataController controller;

    /**
     * @param user     MySQL server username.
     * @param pass     MySQL server password.
     * @param hostname MySQL server's hostname.
     * @param port     MySQL server port.
     * @param database Name of schema/database on MySQL server.
     */
    public Database(DataController controller, String user, String pass, String hostname, String port, String database) {
        this.controller = controller;

        // Create database settings.
        this.user = user;
        this.pass = pass;
        this.url = String.format("jdbc:mysql://%s:%s/", hostname, port) + "?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=GMT%2B11";
        this.database = database;

        // Open the connection.
        connection = open();

        // Check if the connection is valid.
        if (connection == null)
            throw new IllegalStateException("Unable to connect to database!");
    }

    /**
     * Double checks that the JDBC driver is installed.
     *
     * @throws IllegalStateException If the JDBC driver is not installed.
     */
    private void initialize() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            // This will never happen in normal operation, as we include the mysql-connector library with the server JAR.
            throw new IllegalStateException("JDBC driver not found?");
        }
    }

    /**
     * Checks if the MySQL connection is still alive.
     * Re-opens the connection if it is no longer alive.
     */
    private void validate() {
        try {
            if (!connection.isValid(0))
                connection = open();
        } catch (SQLException e) {
            // Impossible.
        }
    }

    /**
     * Attempt to open a connection to the MySQL database.
     *
     * @return Instance of Connection to MySQL database.
     */
    private Connection open() {
        initialize();
        try {
            if (connection == null)
                return DriverManager.getConnection(this.url, this.user, this.pass);
            else if (connection.isValid(3)) // Check the connection is valid
                return connection;
            else {
                // Return a new connection!
                return DriverManager.getConnection(this.url, this.user, this.pass);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Print the stack trace as this should never happen in normal operation.
            throw new IllegalStateException(e.getMessage());
        }
    }

    /**
     * Closes the MySQL connection. Does not attempt to re-open.
     * Unused.
     */
    public void close() {
        // Only close the connection if there is a connection to close.
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
                // This shouldn't happen, so just print the stack trace.
            }
            connection = null;
        }
    }

    /**
     * Creates an injection-safe MySQL Prepared Statement.
     *
     * @param query The MySQL query.
     * @return The Prepared Statement.
     * @see PreparedStatement
     */
    PreparedStatement prepare(String query) {
        // Validate connection first.
        validate();
        try {
            return connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
        } catch (SQLException e) {
            e.printStackTrace();
            // Nothing bad should happen in normal operation, so just print the stack trace.
        }
        return null;
    }

    /**
     * Creates, fills, prepares, executes, and closes an injection-safe MySQL Prepared Statment in one line.
     *
     * @param query     The SQL query.
     * @param escapable The wildcards to escape.
     */
    private void oneLinePrepare(final String query, final Object... escapable) {
        PreparedStatement prep = prepare(query);
        try {
            for (int i = 0; i < escapable.length; i++)
                prep.setObject(i + 1, escapable[i]);
            prep.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
            // Nothing bad should happen in normal operation, so just print the stack trace.
        } finally {
            try {
                prep.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Allows a prepared table creation statement to be executed and closed in one line.
     *
     * @param pstmt The prepared table creation statement.
     * @throws SQLException Catch SQL errors!
     */
    private void oneLineExecute(PreparedStatement pstmt) throws SQLException {
        pstmt.executeUpdate();
        pstmt.close();
    }

    /* Below this line are actual uses of the database. */

    /**
     * Creates the required schema tables if they do not exist.
     */
    public void createTables() throws SQLException {
        DatabaseMetaData meta = connection.getMetaData();

        boolean schemaExists = schemaExists();
        if (!schemaExists) {
            System.out.println(">>> Creating Schema...");
            PreparedStatement schemaCheck = connection.prepareStatement("CREATE DATABASE `" + database + "`");
            schemaCheck.executeUpdate();
            schemaCheck.close();
        }

        // Use this database once we know it exists.
        connection.setCatalog(database);

        // Check if the `Genre` table exists, and then create it if not.
        ResultSet check1 = meta.getTables(database, null, "Genre", null);
        if (!check1.next()) {
            System.out.println(">>> Creating `Genre` table...");
            oneLineExecute(connection.prepareStatement(
                    "CREATE TABLE Genre (" +
                            "`genre` CHAR(20) NOT NULL," +
                            "PRIMARY KEY(`genre`));"
            ));
        }
        check1.close();

        // Check if the `Movie` table exists, and then create it if not.
        ResultSet check2 = meta.getTables(database, null, "Movie", null);
        if (!check2.next()) {
            System.out.println(">>> Creating `Movie` table...");
            oneLineExecute(connection.prepareStatement(
                    "CREATE TABLE Movie (" +
                            "`name` CHAR(60) NOT NULL," +
                            "`running_time` SMALLINT UNSIGNED NOT NULL," +
                            "`release_year` SMALLINT UNSIGNED NOT NULL," +
                            "`genre` CHAR(20) NOT NULL," +
                            "CHECK (`running_time` > 0)," +
                            "CHECK (`release_year` > 1900)," +
                            "PRIMARY KEY(`name`)," +
                            "FOREIGN KEY (`genre`) REFERENCES Genre(`genre`));"
            ));
        }
        check2.close();

        // Check if the `Venue` table exists, and then create it if not.
        ResultSet check3 = meta.getTables(database, null, "Venue", null);
        if (!check3.next()) {
            System.out.println(">>> Creating `Venue` table...");
            oneLineExecute(connection.prepareStatement(
                    "CREATE TABLE Venue (" +
                            "`venue_no` SMALLINT UNSIGNED NOT NULL," +
                            "`no_of_rows` TINYINT UNSIGNED NOT NULL DEFAULT 18," +
                            "`no_of_cols` TINYINT UNSIGNED NOT NULL DEFAULT 30," +
                            "CHECK(`no_of_rows` > 0)," +
                            "CHECK(`no_of_cols` > 0)," +
                            "CHECK(`venue_no` > 0)," +
                            "PRIMARY KEY(`venue_no`));"
            ));
        }
        check3.close();

        // Check if the `Screening` table exists, and then create it if not.
        ResultSet check4 = meta.getTables(database, null, "Screening", null);
        if (!check4.next()) {
            System.out.println(">>> Creating `Screening` table...");
            oneLineExecute(connection.prepareStatement(
                    "CREATE TABLE Screening (\n" +
                            "`start_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                            "`end_date` TIMESTAMP NOT NULL," +
                            "`movie_name` CHAR(60) NOT NULL," +
                            "`screening_id` INT UNSIGNED NOT NULL UNIQUE AUTO_INCREMENT," +
                            "`venue_no` SMALLINT UNSIGNED NOT NULL," +
                            "CHECK(`start_date` < `end_date`)," +
                            "PRIMARY KEY(`start_date`, `end_date`, `movie_name`)," +
                            "FOREIGN KEY (`movie_name`) REFERENCES Movie (`name`)," +
                            "FOREIGN KEY (`venue_no`) REFERENCES Venue (`venue_no`));"
            ));
        }
        check4.close();

        // Check if the `Screening_Times` table exists, and then create it if not.
        ResultSet check5 = meta.getTables(database, null, "Screening_Times", null);
        if (!check5.next()) {
            System.out.println(">>> Creating `Screening_Times` table...");
            oneLineExecute(connection.prepareStatement(
                    "CREATE TABLE Screening_Times (" +
                            "`screening_id` INT UNSIGNED NOT NULL," +
                            "`screening_time` CHAR(5) NOT NULL," +
                            "`screening_day` CHAR(9) NOT NULL," +
                            "PRIMARY KEY(`screening_id`, `screening_time`, `screening_day`)," +
                            "FOREIGN KEY (`screening_id`) REFERENCES Screening(`screening_id`));"
            ));
        }
        check5.close();

        // Check if the `Ticket` table exists, and then create it if not.
        ResultSet check6 = meta.getTables(database, null, "Ticket", null);
        if (!check6.next()) {
            System.out.println(">>> Creating `Ticket` table...");
            oneLineExecute(connection.prepareStatement(
                    "CREATE TABLE Ticket (" +
                            "`screening_id` INT UNSIGNED NOT NULL," +
                            "`selected_date` TIMESTAMP NOT NULL," +
                            "`allocated_seat` CHAR(3) NOT NULL," +
                            "`username` CHAR(16) NOT NULL," +
                            "PRIMARY KEY(`screening_id`, `selected_date`, `allocated_seat`)," +
                            "FOREIGN KEY (`screening_id`) REFERENCES Screening(`screening_id`));"
            ));
        }
        check6.close();

        // Also pre-fill tables with test data if schema was just created.
        if (!schemaExists) {
            System.out.println(">>> Pre-filling tables with test data...");
            Prefill.prefill(this);
        }
    }

    /**
     * @return True, if the database exists already.
     */
    private boolean schemaExists() throws SQLException {
        ResultSet resultSet = connection.getMetaData().getCatalogs();
        while (resultSet.next())
            if (resultSet.getString(1).equals(database)) {
                resultSet.close();
                return true;
            }
        resultSet.close();
        return false;
    }

    /**
     * Loads existing movies from the database into the program.
     *
     * @param movies Provided list that the loaded movies will be added in to.
     */
    public void loadMovies(HashMap<String, Movie> movies) throws SQLException {
        // This SQL query selects all movies and their genres, which is in a separate table.
        ResultSet result = prepare("SELECT * FROM `Movie` NATURAL JOIN `Genre` WHERE 1").executeQuery();

        while (result.next()) {
            // Retrieve all of the attributes and re-construct the model for each row.
            String name = result.getString("name");
            String genre = result.getString("genre");
            int runningTime = result.getInt("running_time");
            int releaseYear = result.getInt("release_year");

            // Add it to the list of loaded movies.
            Movie movie = new Movie(name, genre, runningTime, releaseYear);
            movies.put(name, movie);
        }
        System.out.println(String.format(">>> Loaded %s movies!", movies.size()));
        result.close();
    }

    /**
     * Loads existing venues from the database into the program.
     *
     * @param venues Provided map that the loaded venues will be added in to.
     */
    public void loadVenues(HashMap<Integer, Venue> venues) throws SQLException {
        ResultSet result = prepare("SELECT * FROM `Venue` WHERE 1").executeQuery();

        while (result.next()) {
            // Retrieve all of the attributes and re-construct the model for each row.
            int venueNum = result.getInt("venue_no");
            int noOfRows = result.getInt("no_of_rows");
            int noOfCols = result.getInt("no_of_cols");

            // Add it to the list of loaded venues.
            Venue venue = new Venue(venueNum, noOfRows, noOfCols);
            venues.put(venueNum, venue);
        }
        System.out.println(String.format(">>> Loaded %s venues!", venues.size()));
        result.close();
    }

    /**
     * Loads valid screenings that are in their active date range from the database into the program.
     * This also includes the screening times that are closely related to the screenings.
     *
     * @param screenings    Provided map that the loaded screenings will be added in to.
     * @param currentGenres Provided map that tracks what genres exist in the current set of screenings.
     */
    public void loadScreenings(HashMap<Integer, Screening> screenings, HashMap<String, Integer> currentGenres) throws SQLException {
        // This SQL query selects all screenings and their screening times, which are in a separate table.
        // Do not load in screenings that a. are not in their date range yet or b. have left their date range.
        ResultSet result = prepare("SELECT * FROM `Screening` NATURAL JOIN `Screening_Times` WHERE `start_date` < CURRENT_TIMESTAMP AND `end_date` > CURRENT_TIMESTAMP ORDER BY `screening_id`").executeQuery();

        // Due to the natural join, rows will be duplicated so simply grab every unique instance of a screening.
        while (result.next()) {
            Timestamp startDate = result.getTimestamp("start_date");
            Timestamp endDate = result.getTimestamp("end_date");
            Movie movie = controller.findMovieByTitle(result.getString("movie_name"));
            Venue venue = controller.findVenueByID(result.getInt("venue_no"));

            // Check to see if this genre has been seen yet; add it if it hasn't.
            String genre = movie.getGenre();
            if (!currentGenres.containsKey(genre))
                currentGenres.put(genre, currentGenres.size());

            List<ScreeningTime> times = new ArrayList<>();
            // Continually loop until we have found all screening times for this screening, or if we reach the end.
            final int realID = result.getInt("screening_id");
            while (!result.isAfterLast() && realID == result.getInt("screening_id")) {
                // Add the next screening time for this screening.
                String screeningDay = result.getString("screening_day");
                String screeningTime = result.getString("screening_time");
                ScreeningTime time = new ScreeningTime(screeningDay, screeningTime);
                times.add(time);

                // Look ahead to see if the next set of screening times is also for this screening.
                result.next();
            }
            // Revert look-ahead mentioned above.
            result.previous();

            // Add this to the list of loaded screenings.
            Screening screening = new Screening(movie, venue, startDate, endDate, realID, times);
            screenings.put(realID, screening);
        }
        System.out.println(String.format(">>> Loaded %s screenings!", screenings.size()));
        result.close();

        if (screenings.size() == 0) {
            JOptionPane.showMessageDialog(null, "Whoa, slow down there! You can't use this program yet.\n" +
                            "Please add some screenings and screening times first.\nThis program will shut down after you close this dialog.",
                    "Insufficient Data!", JOptionPane.WARNING_MESSAGE);
            System.exit(0);
        }
    }

    /**
     * Loads in-date, booked tickets from the database into the program.
     *
     * @param tickets Provided list that the loaded tickets will be added in to.
     */
    public void loadTickets(List<Ticket> tickets) throws SQLException {
        // Don't load in this data if the selected date has passed;it is no longer valid. It can stay in the table however!
        ResultSet result = prepare("SELECT * FROM `Ticket` WHERE `selected_date` > CURRENT_TIMESTAMP").executeQuery();

        while (result.next()) {
            // Retrieve all of the attributes and re-construct the model for each row.
            Screening screening = controller.findScreeningByID(result.getInt("screening_id"));
            Timestamp selectedDate = result.getTimestamp("selected_date"); // Load as string then convert using valueOf to stop time zone anomalies.
            String allocatedSeat = result.getString("allocated_seat");
            String username = result.getString("username");

            // Add it to the list of valid tickets.
            Ticket ticket = new Ticket(screening, selectedDate, allocatedSeat, username);
            tickets.add(ticket);
        }
        System.out.println(String.format(">>> Found %s tickets!", tickets.size()));
        result.close();
    }

    /**
     * Saves a recently-created ticket into the database so that it persists indefinitely.
     *
     * @param toSave The ticket to save.
     */
    public void saveTicket(Ticket toSave) {
        oneLinePrepare("INSERT INTO `Ticket` (`screening_id`, `selected_date`, `allocated_seat`, `username`) VALUES (?,?,?,?);",
                toSave.getScreening().getID(), toSave.getSelectedDate(), toSave.getAllocatedSeat(), toSave.getUsername());
    }

    /**
     * Used to safely delete a screening, including all tickets and screening times related to it.
     *
     * @param screeningID The screening ID to safely delete.
     */
    public void safeDeleteScreening(int screeningID) {
        oneLinePrepare("DELETE FROM `Screening_Times` WHERE `screening_id`=?;" +
                        "DELETE FROM `Ticket` WHERE `screening_id`=?;" +
                        "DELETE FROM `Screening` WHERE  `screening_id`=?;",
                screeningID, screeningID, screeningID);
    }

    /**
     * Deletes a singular existing data from the database with full precision.
     *
     * @param ticket The data to delete.
     */
    public void deleteTicket(Ticket ticket) {
        oneLinePrepare("DELETE FROM `Ticket` WHERE `screening_id`=? AND `selected_date`=? AND `allocated_seat`=?",
                ticket.getScreening().getID(), ticket.getSelectedDate(), ticket.getAllocatedSeat());
    }
}