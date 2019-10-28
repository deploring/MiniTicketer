package solar.rpg.ticketer.models;

import java.sql.Timestamp;
import java.util.List;

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
