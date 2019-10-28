package solar.rpg.ticketer.models;

import java.sql.Timestamp;

/**
 * Represents the 'Ticket' model in the architecture.
 * A ticket represents a seat booking at a particular screening at a particular time.
 * All of its attributes are defined in this class.
 *
 * @author Joshua Skinner
 * @version 1.0
 * @see Screening
 * @since 0.1
 */
public class Ticket {

    private final Screening screening;
    private final Timestamp selectedDate;
    private final String allocatedSeat, username;

    public Ticket(Screening screening, Timestamp selectedDate, String allocatedSeat, String username) {
        this.screening = screening;
        this.selectedDate = selectedDate;
        this.allocatedSeat = allocatedSeat;
        this.username = username;
    }

    /**
     * @return The screening that this ticket is booked for.
     */
    public Screening getScreening() {
        return screening;
    }

    /**
     * @return The specific screening date that this ticket is booked for.
     */
    public Timestamp getSelectedDate() {
        return selectedDate;
    }

    /**
     * @return The seat allocated to this ticket, e.g. seat B17.
     */
    public String getAllocatedSeat() {
        return allocatedSeat;
    }

    /**
     * @return Username of the customer who purchased this ticket.
     */
    public String getUsername() {
        return username;
    }

    @Override
    public boolean equals(Object obj) {
        // Tickets are equal if they share the same values for all private variables.
        if (!(obj instanceof Ticket)) return false;
        if (obj == this) return true;
        Ticket ticket = (Ticket) obj;
        return ticket.screening.equals(this.screening) && ticket.selectedDate.equals(this.selectedDate) && ticket.allocatedSeat.equals(this.allocatedSeat);
    }
}
