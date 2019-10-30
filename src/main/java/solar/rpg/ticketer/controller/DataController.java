package solar.rpg.ticketer.controller;

import solar.rpg.ticketer.data.Configuration;
import solar.rpg.ticketer.data.Database;
import solar.rpg.ticketer.models.Movie;
import solar.rpg.ticketer.models.Screening;
import solar.rpg.ticketer.models.Ticket;
import solar.rpg.ticketer.models.Venue;
import solar.rpg.ticketer.views.MainView;

import javax.swing.*;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This controller is responsible for:
 * <ul>
 * <li>Orchestrating database operations.</li>
 * <li>Maintaining loaded movies, screenings, and venues.</li>
 * <li>Creating, removing, and maintaining ticket instances.</li>
 * <li>Performing miscellaneous calculation operations.</li>
 * </ul>
 *
 * @author Joshua Skinner
 * @version 1.0
 * @since 0.1
 */
public class DataController {

    // Useful data formatters; re-usable so initialise them once.
    private static final DateFormat DAY_OF_WEEK_FORMATTER = new SimpleDateFormat("EEEE");
    private static final DateFormat MONTH_FORMATTER = new SimpleDateFormat("MMMM");
    private static final DateFormat TIME_FORMATTER = new SimpleDateFormat("hh:mma");

    private final MainView main;

    // Data sources.
    private Configuration config;
    private Database database;

    // This controller stores all the model states, and it is not freely available.
    private final HashMap<String, Integer> currentGenres;
    private final HashMap<String, Movie> movies;
    private final HashMap<Integer, Venue> venues;
    private final HashMap<Integer, Screening> screenings;
    private final List<Ticket> tickets;

    public DataController(MainView main) throws IllegalStateException {
        this.main = main;
        this.currentGenres = new HashMap<>();
        this.movies = new HashMap<>();
        this.venues = new HashMap<>();
        this.screenings = new HashMap<>();
        this.tickets = new ArrayList<>();

        // Read configuration for MySQL database configuration settings.
        System.out.println("> Reading configuration...");
        try {
            config = new Configuration("ticketer.settings", "mysql_user", "mysql_pass", "mysql_host", "mysql_port", "mysql_database");
        } catch (IllegalArgumentException ex) {
            // Configuration was not loaded. Do not continue with execution.
            System.out.println(String.format("!! Unable to load configuration: %s", ex.getMessage()));
            JOptionPane.showMessageDialog(null, String.format("Unable to read your ticketer.settings file, reason:\n" +
                            "%s\nPlease check your configuration. The program will exit after closing this dialog.", ex.getMessage()),
                    "Unable to Read Configuration!", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
            return;
        }
        System.out.println("...success!");

        // Attempt to connect to database and load required data.
        System.out.println("> Connecting to database...");
        try {
            database = new Database(this, config.getString("mysql_user"), config.getString("mysql_pass"), config.getString("mysql_host"), config.getString("mysql_port"), config.getString("mysql_database"));

            // Load in all the data from the database.
            System.out.println(">> Checking tables...");
            database.createTables();
            System.out.println(">> Loading available Movies...");
            database.loadMovies(movies);
            System.out.println(">> Loading available Venues...");
            database.loadVenues(venues);
            System.out.println(">> Loading available Screenings...");
            database.loadScreenings(screenings, currentGenres);
            System.out.println(">> Loading purchased Tickets...");
            database.loadTickets(tickets);

            // Perform cross-validation routines to ensure that data is integrous.
            System.out.println(">> Performing cross-validation...");
            crossValidate();
        } catch (SQLException | IllegalStateException e) {
            // Database was not loaded. Do not continue with execution.
            JOptionPane.showMessageDialog(null, String.format("Unable to connect to database at '%s:%s'. Reason:\n%s\n" +
                            "Please double-check MySQL server settings in the 'ticketer.settings' file.\n" +
                            "The program will exit after you close this dialog.", config.getString("mysql_host"),
                    config.getString("mysql_port"), e.getMessage()), "Unable to Connect to Database!", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
            return;
        }
        System.out.println("...success!");
    }

    // Setting this to true will remove erroneous rows from the database permanently, otherwise they are just logged and removed at runtime.
    private static final boolean DELETE_CORRUPTED_ROWS = false;

    /**
     * Performs cross-validation of all attributes to make sure there is
     * no overlapping, duplications, discrepancies, or other anomalies that
     * cannot easily be detected with SQL table constraints.
     * <p>
     * This is only done at startup, and is to prevent corruption through
     * an external database editing tool, such as phpMyAdmin.
     */
    private void crossValidate() {
        // Ensure screenings of the same movie do not have overlapping date ranges.
        List<Integer> invalid = new ArrayList<>();
        screenings.values().forEach(first -> {
            // Don't re-compare screenings that have been marked as invalid.
            // Ignore exact copies, only compare against others.
            // Only compare screenings with the same movie title.
            // If one screening's end date comes after the other's start date, it is invalid.
            // The reverse is also true; if one's start date comes before the other's end date, it is also invalid.
            screenings.values().stream().filter(second -> !invalid.contains(first.getID()) && !invalid.contains(second.getID()))
                    .filter(second -> first != second).filter(second -> first.getMovie().equals(second.getMovie()))
                    .filter(second -> first.getEndDate().after(second.getStartDate()) || first.getStartDate().before(second.getEndDate()))
                    .forEach(second -> invalid.add(second.getID()));
        });
        // Purge overlapping screenings (only one is selected to be removed, not both).
        // Remove all local copies pertaining to this invalid screening.
        // If enabled, reflect the exact same changes in the database also so they do not come back next restart.
        if (invalid.size() != 0)
            invalid.forEach(ID -> {
                System.out.println(">> WARNING!! Screening ID #%s has been detected overlapping with other screenings. It has been removed as a result.");
                screenings.remove(ID);
                tickets.removeAll(findTicketsByScreening(ID));
                if (DELETE_CORRUPTED_ROWS)
                    database.safeDeleteScreening(ID);
            });

        // Ensure Tickets fall within the date ranges of their screenings.
        List<Ticket> invalidTickets = new ArrayList<>();
        // Invalid tickets start before or after their screening's date range.
        tickets.forEach(temp -> {
            Screening screening = temp.getScreening();
            if (temp.getSelectedDate().before(screening.getStartDate()) || temp.getSelectedDate().after(screening.getEndDate()))
                invalidTickets.add(temp);
        });
        // Purge invalid tickets.
        // If enabled, delete the invalid tickets in the database so that they do not re-appear on restart.
        if (invalidTickets.size() != 0)
            invalidTickets.forEach(temp -> {
                System.out.println(">> WARNING!! A data was found outside of its screening's date range and was removed! Perhaps you were messing around with the timestamps in phpMyAdmin?");
                if (DELETE_CORRUPTED_ROWS)
                    database.deleteTicket(temp);
            });
        // Remove all local copies of all invalid tickets.
        tickets.removeAll(invalidTickets);
    }

    /**
     * Deletes a ticket- both locally and externally.
     *
     * @param toDelete Ticket to delete.
     */
    public void deleteTicket(Ticket toDelete) {
        tickets.remove(toDelete);
        database.deleteTicket(toDelete);
    }

    /**
     * Deletes a list of tickets.
     *
     * @param toDelete The tickets to delete.
     * @see #deleteTicket(Ticket)
     */
    public void deleteTickets(List<Ticket> toDelete) {
        toDelete.forEach(this::deleteTicket);
    }

    /**
     * Attempts to retrieve a Movie by its title.
     *
     * @param movie The movie's title.
     * @return The movie, if found, otherwise null.
     */
    public Movie findMovieByTitle(String movie) {
        return movies.get(movie);
    }

    /**
     * Attempts to retrieve a Venue by its number.
     *
     * @param venue The venue number.
     * @return The venue, if found, otherwise null.
     */
    public Venue findVenueByID(int venue) {
        return venues.get(venue);
    }

    /**
     * Attempts to retrieve a Screening by its unique identifier.
     *
     * @param screening The screening ID.
     * @return The screening, if found, otherwise null.
     */
    public Screening findScreeningByID(int screening) {
        return screenings.get(screening);
    }

    /**
     * @return The currently selected screening.
     */
    public Screening getSelectedScreening() {
        return findScreeningByID(main.state().getSelectedScreening());
    }

    /**
     * @param screening The unique identifier of a particular screening.
     * @return All tickets that have been booked for this screening.
     */
    private List<Ticket> findTicketsByScreening(int screening) {
        return tickets.stream().filter(temp -> temp.getScreening().getID() == screening).collect(Collectors.toList());
    }

    /**
     * Finds all tickets of a particular screening for a particular time slot.
     *
     * @param screening The screening in question.
     * @param date      The particular time slot in question.
     * @return All tickets that match the above two criteria.
     */
    public List<Ticket> findTicketsByScreeningAndTime(int screening, Timestamp date) {
        return tickets.stream().filter(temp -> temp.getScreening().getID() == screening && temp.getSelectedDate().equals(date)).collect(Collectors.toList());
    }

    /**
     * Finds all tickets of a particular screening for a username.
     *
     * @param username Username that was used to make the ticket booking.
     * @return All tickets that belong to the username.
     */
    public List<Ticket> findTicketsByUsername(String username) {
        return tickets.stream().filter(temp -> temp.getUsername().equals(username)).collect(Collectors.toList());
    }

    /**
     * Determines the number of remaining seats for a specific screening date.
     *
     * @param screening The screening to check.
     * @param date      The particulate date & time of the screening.
     * @return Number of seats available in this screening at the specified time.
     */
    public int calculateNumberOfAvailableSeats(Screening screening, Timestamp date) {
        int remaining = screening.getVenue().getTotalSeats();
        for (Ticket ticket : tickets)
            if (ticket.getScreening().equals(screening) && ticket.getSelectedDate().equals(date))
                remaining--;
        return remaining;
    }

    /**
     * @return A collection of the current active screenings.
     */
    Collection<Screening> getScreenings() {
        return screenings.values();
    }

    /**
     * @return List of genres found in the loaded screenings.
     */
    public Set<Map.Entry<String, Integer>> getCurrentGenreSet() {
        return currentGenres.entrySet();
    }

    /**
     * Calculates all potential booking times within the next 14 days for a screening.
     *
     * @return The list of available booking times.
     */
    public LinkedList<Timestamp> calculateTimes(Screening screening) {
        // Set up minimum, maximum, and current timestamps.
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());

        // Go ahead 14 days to determine maximum then go back.
        cal.add(Calendar.DAY_OF_WEEK, 14);
        Timestamp max = new Timestamp(cal.getTime().getTime());
        cal.setTime(new Date());

        // If calculated maximum occurs after end of screening range, set it to that instead.
        if (max.after(screening.getEndDate())) max = screening.getEndDate();

        LinkedList<Timestamp> result = new LinkedList<>();
        // Continually loop, adding 1 day each time until we've determined all reasonable days.
        while (max.after(cal.getTime())) {
            // Determine the calendar time of now.
            Calendar now = Calendar.getInstance();
            now.setTime(cal.getTime());
            now.set(Calendar.SECOND, 0);
            now.set(Calendar.MILLISECOND, 0);
            String dayOfWeek = DAY_OF_WEEK_FORMATTER.format(now.getTime());

            // For every incremental day, check what screening times fall upon this day.
            screening.getScreeningTimes().stream().filter(time -> time.getDayOfWeek().equals(dayOfWeek)).forEach(time -> {
                try {
                    // Determine time of day that the screening will take place, then add it.
                    String[] times = time.getTime().split(":");
                    int hours = Integer.parseInt(times[0]), minutes = Integer.parseInt(times[1]);
                    now.set(Calendar.HOUR_OF_DAY, hours);
                    now.set(Calendar.MINUTE, minutes);

                    // Only add this timestamp after checking that it hasn't passed yet.
                    Timestamp timestamp = new Timestamp(now.getTime().getTime());
                    if (timestamp.before(new Date())) return;
                    result.add(timestamp);
                } catch (Exception ex) {
                    System.out.println("> WARNING: Unable to process time value '" + time.getTime() + "' for screening ID #" + screening.getID());
                }
            });
            cal.add(Calendar.DAY_OF_WEEK, 1);
        }
        return result;
    }

    /**
     * Converts a timestamp into a human-friendly string:
     * "day name, nth of month @ hh:mm am/pm"
     * e.g. "Wednesday, 30th of October, 01:00pm"
     *
     * @param calculatedDate A pre-calculated screening date, ideally.
     * @return Human friendly formatted date as explained above.
     */
    public String friendlyDate(Date calculatedDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(calculatedDate);
        String dayOfWeek = DAY_OF_WEEK_FORMATTER.format(cal.getTime());
        String dayNum = ordinal(cal.get(Calendar.DAY_OF_MONTH));
        String month = MONTH_FORMATTER.format(cal.getTime());
        String time = TIME_FORMATTER.format(cal.getTime());
        return dayOfWeek + ", " + dayNum + " of " + month + ", " + time;
    }

    // Ordinal suffixes.
    private static final String[] SUFFIXES = new String[]{"th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th"};

    /**
     * Utility method to calculate ordinals.
     *
     * @param i The number.
     * @return The ordinal of the number.
     */
    private String ordinal(int i) {
        switch (i % 100) {
            case 11:
            case 12:
            case 13:
                return i + "th";
            default:
                return i + SUFFIXES[i % 10];
        }
    }

    /**
     * Converts a worded seat allocation, e.g. B12, to an array index position.
     * Seat B12 in this case would become [2, 12].
     *
     * @param seatAlloc Worded seat allocation.
     * @return Array index position.
     */
    public int[] seatAllocToArrayPos(String seatAlloc) {
        // This method is unused, but I will keep it here just in case.
        int row = seatAlloc.charAt(0) - 'A';
        int col = Integer.parseInt(seatAlloc.substring(1)) - 1;
        return new int[]{row, col};
    }

    /**
     * Converts a seat's array index position into a worded seat allocation.
     * e.g. [2, 12] will become seat B12.
     *
     * @param seatArrayPos Array index position.
     * @return Worded seat allocation.
     */
    public String seatArrayPosToAlloc(int[] seatArrayPos) {
        char row = (char) ('A' + seatArrayPos[0]);
        return row + "" + (seatArrayPos[1] + 1);
    }

    /**
     * Once a user clicks "Make Booking", this method creates
     * the tickets and reflects the changes in the database.
     */
    public void compileBooking() {
        // First, create all the tickets.
        List<Ticket> result = new ArrayList<>();
        for (Iterator<String> it = main.state().seatIterator(); it.hasNext(); ) {
            String seat = it.next();
            Ticket ticket = new Ticket(getSelectedScreening(), main.state().getSelectedTime(), seat, main.state().getBookingUsername());
            result.add(ticket);
        }
        // Add them all to the existing ticket list too!
        tickets.addAll(result);

        // Second, insert all the tickets into the database.
        result.forEach(ticket -> database.saveTicket(ticket));

        // Send a message to the user!
        JOptionPane.showMessageDialog(null, "Your booking has been saved & confirmed!\nTo view your tickets, please click \"View Tickets\".\nThank you!", "Booking Success!", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Re-maps a username query ticket list into something more easily presentable.
     * In this case, it takes each tick it, and groups them by screening, or rather, by movie.
     * Grouping is done by movie because it is simplier and two screenings of the same movie cannot happen.
     *
     * @return Movies, mapped to individual lists of tickets.
     * @see #findTicketsByUsername(String)
     */
    public HashMap<Movie, List<Ticket>> remapTickets() {
        HashMap<Movie, List<Ticket>> result = new HashMap<>();

        main.state().getQueryTickets().forEach(ticket -> {
            Movie movie = ticket.getScreening().getMovie();
            List<Ticket> ticketsInMovie = result.getOrDefault(movie, new ArrayList<>());
            if (ticketsInMovie.isEmpty()) result.put(movie, ticketsInMovie);
            ticketsInMovie.add(ticket);
        });
        return result;
    }
}
