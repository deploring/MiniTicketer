package solar.rpg.ticketer.models;

/**
 * Represents the 'Venue' model in the architecture.
 * A venue is a location where a screening is shown.
 * All of its attributes are defined in this class.
 *
 * @author Joshua Skinner
 * @version 1.0
 * @see Screening
 * @since 0.1
 */
public class Venue {

    private final int venueNum, noOfRows, noOfCols;

    public Venue(int venueNum, int noOfRows, int noOfCols) {
        this.venueNum = venueNum;
        this.noOfRows = noOfRows;
        this.noOfCols = noOfCols;
    }

    /**
     * @return This venue's number, it acts as a unique identifier too.
     */
    public int getVenueNum() {
        return venueNum;
    }

    /**
     * @return Number of seat rows, which are designated by a letter.
     */
    public int getNoOfRows() {
        return noOfRows;
    }

    /**
     * @return Number of seat columns, which are designated by a number.
     */
    public int getNoOfCols() {
        return noOfCols;
    }

    /**
     * @return Total number of available seats at this venue.
     */
    public int getTotalSeats() {
        return noOfRows * noOfCols;
    }

    @Override
    public boolean equals(Object obj) {
        // Venues are equal if they share the same venue number.
        if (!(obj instanceof Venue)) return false;
        if (obj == this) return true;
        return ((Venue) obj).venueNum == this.venueNum;
    }
}
