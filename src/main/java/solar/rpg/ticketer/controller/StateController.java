package solar.rpg.ticketer.controller;

import solar.rpg.ticketer.models.Screening;
import solar.rpg.ticketer.models.Ticket;
import solar.rpg.ticketer.views.MainView;
import solar.rpg.ticketer.views.booking.ArrangementView;

import javax.swing.*;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This controller is responsible for controlling & maintaining the <em>logical</em>
 * state of the booking progress from the other views, as opposed to maintaining existing data.
 *
 * @author Joshua Skinner
 * @version 1.0
 * @see DataController
 * @since 1.0
 */
public class StateController {

    // Reference to MainView.
    private final MainView main;

    // Movie Grid View page & screening selection states.
    private int selectedPage = 1;
    private int selectedScreening = -1;

    // Calculations for regular grid view.
    private final int maxPage;
    private final int lastPageModulo;

    // Calculations & elements for sorted grid view.
    private String sortBy = "";
    private final LinkedList<Screening> sortedScreenings;
    private int maxSortedPage;
    private int lastSortedPageModulo;

    // Current state of booking arrangements.
    private ArrangementView.ArrangementState state;
    private LinkedList<Timestamp> availableTimes;
    private Timestamp selectedTime;
    private int noOfAttendees;
    private final Set<String> selectedSeats;
    private String bookingUsername = "";

    // Current state of ticket viewing.
    private String queryUsername;
    private List<Ticket> queryTickets;

    public StateController(MainView main) {
        this.main = main;
        this.sortedScreenings = new LinkedList<>();
        this.selectedSeats = new LinkedHashSet<>();

        // Calculate maximum amount of pages of screenings that can be shown.
        maxPage = (int) Math.ceil(main.data().getScreenings().size() / 6D);
        lastPageModulo = main.data().getScreenings().size() % 6;
    }

    /**
     * @return Currently selected page in the movie grid view.
     */
    public int getSelectedPage() {
        return selectedPage;
    }

    /**
     * Sets the currently selected page in the movie grid view.
     *
     * @param selectedPage Currently selected page.
     */
    public void setSelectedPage(int selectedPage) {
        this.selectedPage = selectedPage;
    }

    /**
     * @return ID of the screening that the user has selected to book.
     */
    public int getSelectedScreening() {
        return selectedScreening;
    }

    /**
     * Sets the ID of the screening that the user has selected to book.
     * Setting a value of -1 indicates nothing is selected.
     *
     * @param selectedScreening ID of the selected screening.
     */
    public void setSelectedScreening(int selectedScreening) {
        this.selectedScreening = selectedScreening;
    }

    /**
     * Updates genre sorting preferences.
     *
     * @param sortBy The new genre to sort by, if any.
     */
    public void changeSortBy(String sortBy) {
        this.sortBy = sortBy;
        if (!sortBy.isEmpty()) {
            // Find all screenings that fit under the genre category.
            sortedScreenings.clear();
            main.data().getScreenings().stream().filter(screening -> screening.getMovie().getGenre().equals(sortBy)).forEach(screening -> sortedScreenings.add(screening));

            // Calculate maximum amount of pages of screenings that can be shown.
            maxSortedPage = (int) Math.ceil(sortedScreenings.size() / 6D);
            lastSortedPageModulo = sortedScreenings.size() % 6;
        }

        // Reflect the changes on the current screening grid output.
        main.booking().movieGrid().reset();
        main.booking().movieGrid().update();
    }

    /**
     * This is unused, but I will keep it anyway.
     *
     * @return What genre the movie grid is being sorted by, if anything.
     */
    public String getSortBy() {
        return sortBy;
    }

    /**
     * Calculates up to 6 screenings to show on the movie grid view.
     * This is based on the current "page", where page 1 represents the first six in the map.
     *
     * @return Screening for page content.
     */
    public LinkedList<Screening> paginate() {
        if (sortBy.isEmpty())
            return paginate(main.data().getScreenings().iterator(), maxPage, lastPageModulo);
        else return paginate(sortedScreenings.iterator(), maxSortedPage, lastSortedPageModulo);
    }

    /**
     * Once the sorted/un-sorted values have been ascertained, pagination can be calculated.
     *
     * @param iterator       The iterator containing the data.
     * @param maxPage        The maximum page.
     * @param lastPageModulo The modulo for the last page.
     * @return Screening for page content.
     */
    private LinkedList<Screening> paginate(Iterator<Screening> iterator, int maxPage, int lastPageModulo) {
        if (getSelectedPage() > maxPage) throw new IllegalArgumentException("Cannot paginate when page > maxPage?");
        LinkedList<Screening> result;

        // Bring the iterator to the correct starting position.
        int startingIndex = Math.max(0, (getSelectedPage() - 1) * 6);
        IntStream.range(0, startingIndex).forEachOrdered(i -> iterator.next());

        // Add screenings to page as long as there is still more to come.
        result = IntStream.range(0, (getSelectedPage() != maxPage || lastPageModulo == 0 ? 6 : lastPageModulo))
                .mapToObj(i -> iterator.next()).collect(Collectors.toCollection(LinkedList::new));
        return result;
    }

    /**
     * @return The maximum page of screening results.
     */
    public int getMaxPage() {
        return sortBy.isEmpty() ? maxPage : maxSortedPage;
    }

    /**
     * @return The current state of the booking arrangement.
     */
    public ArrangementView.ArrangementState getArrangementState() {
        return state;
    }

    /**
     * Sets the current state of the booking arrangement.
     * This can also change other values depending on what is set.
     *
     * @param state Current state of the booking arrangement.
     */
    public void setArrangementState(ArrangementView.ArrangementState state) {
        this.state = state;
        switch (state) {
            case UNDECIDED:
                selectedScreening = -1;
            case DECIDE_WHEN:
                selectedSeats.clear();
                selectedTime = null;
                availableTimes = null;
                noOfAttendees = -1;
                bookingUsername = "";
                break;
            case DECIDE_ATTENDEES:
                noOfAttendees = -1;
                break;
        }
        main.booking().arrangement().update();
    }

    /**
     * Sets the list of available screening times for the selected screening.
     *
     * @param availableTimes Available screening times.
     */
    public void setAvailableTimes(LinkedList<Timestamp> availableTimes) {
        this.availableTimes = availableTimes;
    }

    /**
     * @return List of available screening times for the selected screening.
     */
    public LinkedList<Timestamp> getAvailableTimes() {
        return availableTimes;
    }

    /**
     * @return The selected booking time for the selected screening.
     */
    public Timestamp getSelectedTime() {
        return selectedTime;
    }

    /**
     * Sets the selected booking time for the selected screening.
     *
     * @param selectedTime Selected booking time.
     */
    public void setSelectedTime(Timestamp selectedTime) {
        this.selectedTime = selectedTime;
    }

    /**
     * @return User defined number of attendees for booking-in-progress.
     */
    public int getNoOfAttendees() {
        return noOfAttendees;
    }

    /**
     * Sets the used defined number of attendees for a booking-in-progress.
     *
     * @param noOfAttendees Number of attendees.
     */
    public void setNoOfAttendees(int noOfAttendees) {
        this.noOfAttendees = noOfAttendees;
    }

    /**
     * Adds a seat to the currently selected seats.
     *
     * @param seat The seat to add to the selection set.
     * @return True if seat was not already in the set; otherwise false.
     */
    public boolean addSeatSelection(String seat) {
        if (selectedSeats.size() >= noOfAttendees) {
            JOptionPane.showMessageDialog(null, "You cannot select any more seats! Please proceed to the next section.\nIf you wish to remove a selected seat, please click it again.", "All Seats Selected!", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return selectedSeats.add(seat);
    }

    /**
     * Removes a selected seat from the currently selected seats.
     *
     * @param seat The seat to remove from the selection set.
     * @return True if the seat was in the set; otherwise false.
     */
    public boolean removeSeatSelection(String seat) {
        return selectedSeats.remove(seat);
    }

    /**
     * Checks if a particular seat has been selected by the user.
     *
     * @param seat The seat to check.
     * @return True if seat is selected; false otherwise.
     */
    public boolean hasSelectedSeat(String seat) {
        return selectedSeats.contains(seat);
    }

    /**
     * @return Number of currently selected seats.
     */
    public int getNumOfSelectedSeats() {
        return selectedSeats.size();
    }

    /**
     * Completely removes all selected seats.
     */
    public void resetSeatSelection() {
        selectedSeats.clear();
    }

    /**
     * @return The set of selected seats, as a string.
     */
    public String selectedSeatsAsString() {
        return Arrays.toString(selectedSeats.toArray(new String[0]));
    }

    /**
     * @return An iterator for the selected seats set.
     */
    Iterator<String> seatIterator() {
        return selectedSeats.iterator();
    }

    /**
     * Sets the user-defined username for saving the tickets under.
     *
     * @param bookingUsername The username.
     */
    public void setBookingUsername(String bookingUsername) {
        this.bookingUsername = bookingUsername;
    }

    /**
     * @return The user-defined username to save the tickets under.
     */
    String getBookingUsername() {
        return bookingUsername;
    }

    /**
     * @return The user-defined username to search for existing tickets with.
     */
    public String getQueryUsername() {
        return this.queryUsername;
    }

    /**
     * Sets the user-defined username to search for existing tickets with.
     *
     * @param queryUsername Username to search for tickets.
     */
    public void setQueryUsername(String queryUsername) {
        this.queryUsername = queryUsername;
    }

    /**
     * @return List of tickets found under the query username.
     * @see #getQueryUsername()
     */
    List<Ticket> getQueryTickets() {
        return queryTickets;
    }

    /**
     * Sets the list of found tickets under the query username.
     *
     * @param queryTickets List of found tickets.
     */
    public void setQueryTickets(List<Ticket> queryTickets) {
        this.queryTickets = queryTickets;
    }
}
