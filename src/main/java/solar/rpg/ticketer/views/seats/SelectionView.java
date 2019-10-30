package solar.rpg.ticketer.views.seats;

import solar.rpg.ticketer.models.Screening;
import solar.rpg.ticketer.models.Ticket;
import solar.rpg.ticketer.views.MainView;
import solar.rpg.ticketer.views.util.SpacedJButton;
import solar.rpg.ticketer.views.util.View;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.List;

/**
 * SelectionView is the follow-up view after successfully choosing a movie screening, time slot, and number of attendees.
 * This view is made up of a 2D array of seats, equivalent to the dimensions of the venue that the screening is shown at.
 * The user, based on their input from BookingView, will need to select the appropriate amount of seats using this view.
 * Seat selection can either be done manually by selecting individual seats or automatically by clicking "auto fill".
 *
 * @author Joshua Skinner
 * @version 1.0
 * @see solar.rpg.ticketer.views.booking.BookingView
 * @since 0.1
 */
public class SelectionView extends View {

    // View Elements
    private SeatSelection seatSelection;
    private JButton confirm;

    public SelectionView(MainView main) {
        super(main);
    }

    @Override
    public void generate() {
        // Generate labelled border.
        mainPanel.setLayout(new BorderLayout());

        // Controls is a view on the bottom with an array of useful functions, including an auto-seat-selection button.
        JPanel controls = new JPanel();
        controls.setLayout(new GridLayout(1, 4));
        controls.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));

        // Initialise control buttons.
        SpacedJButton reset = new SpacedJButton("Reset Selection", 5, 10, 5, 10);
        reset.get().addActionListener((e) -> {
            main.state().resetSeatSelection();
            reset();
        });
        SpacedJButton autoSelect = new SpacedJButton("Auto-Select Seats", 5, 10, 5, 10);
        autoSelect.get().addActionListener((e) -> seatSelection.autoSelect());
        SpacedJButton cancel = new SpacedJButton("Cancel Booking", 5, 10, 5, 10);
        cancel.get().addActionListener((e) -> main.updateState(MainView.UIState.INITIAL_BOOKING));
        SpacedJButton confirm = new SpacedJButton("Confirm Selection", 5, 10, 5, 10);
        confirm.get().addActionListener((e) -> {
            // There is no validation required because this button is not enabled unless the correct amount of seats are selected.
            main.updateState(MainView.UIState.CONFIRMATION);
        });
        this.confirm = confirm.get();
        this.confirm.setEnabled(false);
        controls.add(reset);
        controls.add(autoSelect);
        controls.add(cancel);
        controls.add(cancel);
        controls.add(confirm);

        mainPanel.add(controls, BorderLayout.SOUTH);
    }

    /**
     * SeatSelection is a sub-view that represents the grid of seats that the user can select.
     * It is not in an external class because there is no other sub-views to complement this one.
     *
     * @author Joshua Skinner
     */
    private class SeatSelection extends JPanel {

        // Grid dimension information/state.
        private final int rows;
        private final int cols;
        private final JButton[][] seatButtons;

        SeatSelection() {
            // Determine important variables from previous section.
            Screening selected = main.data().getSelectedScreening();
            List<Ticket> existing = main.data().findTicketsByScreeningAndTime(selected.getID(), main.state().getSelectedTime());
            rows = selected.getVenue().getNoOfRows();
            cols = selected.getVenue().getNoOfCols();

            // Change border title of outer view to reflect user selection.
            mainPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY, 1),
                    "Seating Allocation for \"" + selected.getMovie().getName() + "\" (" + selected.getMovie().getReleaseYear() + ") on "
                            + main.data().friendlyDate(main.state().getSelectedTime()) + ", Venue #" + selected.getVenue().getVenueNum(), TitledBorder.CENTER, TitledBorder.TOP));

            setLayout(new GridLayout(rows, cols));
            seatButtons = new JButton[rows][cols];

            // Initialise each selectable button.
            Font buttonFont = new Font("Calibri", Font.PLAIN, 11);
            for (int row = 0; row < rows; row++)
                for (int col = 0; col < cols; col++) {
                    String alloc = main.data().seatArrayPosToAlloc(new int[]{row, col});
                    SpacedJButton seat = new SpacedJButton(alloc, 24, 2, 24, 2);
                    seat.get().setFont(buttonFont);
                    // Check if this seat has been pre-booked.
                    for (Ticket ticket : existing)
                        if (ticket.getAllocatedSeat().equals(alloc)) {
                            seat.get().setBackground(new Color(227, 102, 89));
                            seat.get().setEnabled(false);
                            break;
                        }

                    if (main.state().hasSelectedSeat(alloc)) // Also refresh previous selection if returning from confirmation screen.
                        seat.get().setBackground(new Color(124, 196, 41));

                    // Configure listener to add and remove this seat from the selection.
                    seat.get().addActionListener((e) -> {
                        if (main.state().removeSeatSelection(alloc))
                            seat.get().setBackground(new JButton().getBackground());
                        else if (main.state().addSeatSelection(alloc))
                            seat.get().setBackground(new Color(124, 196, 41));
                        check();
                    });

                    seatButtons[row][col] = seat.get();
                    add(seat);
                }
        }

        /**
         * Discards previous user selection and automatically selects the
         * correct number of seats. The selector will try to select the seats
         * up front first. Though, ideally, in a movie-watching scenario, you'd
         * you'd want to select the seats up back first.
         */
        void autoSelect() {
            // Automatically select seats based on immediate availability.
            main.state().resetSeatSelection();
            for (int row = 0; row < rows; row++)
                for (int col = 0; col < cols; col++) {
                    JButton seat = seatButtons[row][col];
                    if (!seat.isEnabled()) continue; // Ignore already-booked seats.
                    String alloc = main.data().seatArrayPosToAlloc(new int[]{row, col});
                    main.state().addSeatSelection(alloc);

                    // Stop auto-selecting once a seat has been allocated for each attendee & refresh.
                    if (main.state().getNumOfSelectedSeats() == main.state().getNoOfAttendees()) {
                        refresh();
                        check();
                        return;
                    }
                }
        }

        /**
         * Refreshes current state of this view to reflect the currently
         * selected seats, i.e. after auto-selection or clicking reset.
         */
        void refresh() {
            for (int row = 0; row < rows; row++)
                for (int col = 0; col < cols; col++) {
                    JButton seat = seatButtons[row][col];
                    if (!seat.isEnabled()) continue; // Ignore already-booked seats.
                    String alloc = main.data().seatArrayPosToAlloc(new int[]{row, col});
                    if (main.state().hasSelectedSeat(alloc))
                        seat.setBackground(new Color(124, 196, 41));
                    else seat.setBackground(new JButton().getBackground());
                }
        }

        /**
         * Checks if the correct amount of seats have been selected, and enables
         * the 'Confirm' button if this is the case; otherwise the opposite happens.
         */
        void check() {
            if (main.state().getNumOfSelectedSeats() == main.state().getNoOfAttendees())
                confirm.setEnabled(true);
            else confirm.setEnabled(false);
        }
    }

    @Override
    public void reset() {
        seatSelection.refresh();
    }

    @Override
    public void update() {
        seatSelection = new SeatSelection();
        try {
            mainPanel.remove(1);
        } catch (IndexOutOfBoundsException ignored) {
            // It may not exist, but still attempt to remove the previous seat selection view if possible.
        }
        mainPanel.add(seatSelection, BorderLayout.CENTER, 1);
    }
}
