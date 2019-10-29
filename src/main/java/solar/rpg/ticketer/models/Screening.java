package solar.rpg.ticketer.models;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Represents the 'Screening' model in the architecture.
 * A screening shows movies at certain times on certain days for a given time period.
 * All of its attributes are defined in this class.
 *
 * @author Joshua Skinner
 * @version 1.0
 * @see Movie
 * @see Venue
 * @see Ticket
 * @see ScreeningTime
 * @since 0.1
 */
public class Screening {

    private final Movie movie;
    private final Venue venue;
    private final Timestamp startDate, endDate;
    private final int ID;
    private final List<ScreeningTime> screeningTimes;

    public Screening(Movie movie, Venue venue, Timestamp startDate, Timestamp endDate, int ID, List<ScreeningTime> screeningTimes) {
        this.movie = movie;
        this.venue = venue;
        this.startDate = startDate;
        this.endDate = endDate;
        this.ID = ID;
        this.screeningTimes = screeningTimes;
    }

    /**
     * @return The movie that this screening is showing.
     */
    public Movie getMovie() {
        return movie;
    }

    /**
     * @return The venue that this screening is being held at.
     */
    public Venue getVenue() {
        return venue;
    }

    /**
     * @return Start date of the date range of this screening.
     */
    public Timestamp getStartDate() {
        return startDate;
    }

    /**
     * @return End date for the date range of this screening.
     */
    public Timestamp getEndDate() {
        return endDate;
    }

    /**
     * Sums up in casual terms how close the screening is to being over.
     *
     * @return Time status in casual terms.
     */
    public String timeStatus() {
        long diff = getEndDate().getTime() - System.currentTimeMillis();
        long days = TimeUnit.MILLISECONDS.toDays(diff);
        if (TimeUnit.MILLISECONDS.toHours(diff) <= 6) return "less than six hours";
        else if (days <= 1) return "less than a day";
        else if (days <= 7) return "less than a week";
        else if (days <= 14) return days + " days";
        else if (days <= 21) return "more than a couple of weeks";
        else if (days <= 28) return "about a month";
        else return "more than a month";
    }

    /**
     * @return The unique identifier associated with this screening.
     */
    public int getID() {
        return ID;
    }

    /**
     * @return A list of each day and time for this screening.
     */
    public List<ScreeningTime> getScreeningTimes() {
        return screeningTimes;
    }

    @Override
    public boolean equals(Object obj) {
        // Screenings are equal if they share the same identification number.
        if (!(obj instanceof Screening)) return false;
        if (obj == this) return true;
        return ((Screening) obj).ID == this.ID;
    }
}
