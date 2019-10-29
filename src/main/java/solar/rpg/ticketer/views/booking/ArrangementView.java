package solar.rpg.ticketer.views.booking;

import solar.rpg.ticketer.models.Screening;
import solar.rpg.ticketer.models.Ticket;
import solar.rpg.ticketer.views.MainView;
import solar.rpg.ticketer.views.util.SpacedJButton;
import solar.rpg.ticketer.views.util.View;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * ArrangementView is a sub-view of BookingView
 * This view allows a user, after selecting a movie to watch, to proceed with the booking process.
 * This includes selecting a suitable time slot, and specifying the number of attendees.
 * There are also some miscellaneous functions to hard-reset the whole screen and view current tickets.
 *
 * @author Joshua Skinner
 * @version 1.0
 * @since 0.1
 */
public class ArrangementView extends View {

    // Reference to BookingView
    private BookingView booking;

    // View elements.
    private JTextPane lStep1, lStep2, lStep3;
    private JTextField tStep2;
    private JButton cancelAll, confirmNumbers, pickSeats;
    private JComboBox<String> availableTimes;

    ArrangementView(MainView main, BookingView booking) {
        super(main);
        this.booking = booking;
    }

    @Override
    public void generate() {
        // Give the main panel an empty border to emulate padding & margins.
        mainPanel.setLayout(new GridLayout(4, 1));
        mainPanel.setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(6, 6, 6, 6), BorderFactory.createLineBorder(Color.GRAY, 1)));

        // Initialise UI elements for step 1 (picking a date and time).
        lStep1 = new JTextPane();
        JPanel step1 = createTextSection(new BorderLayout(), "1. Select Date & Time", lStep1, BorderLayout.CENTER);
        availableTimes = new JComboBox<>();
        availableTimes.addItemListener((e) -> {
            if (availableTimes.getSelectedIndex() == 0) {
                main.state().setArrangementState(ArrangementState.DECIDE_WHEN);
                return;
            }
            Timestamp selected = main.state().getAvailableTimes().get(availableTimes.getSelectedIndex() - 1);
            main.state().setSelectedTime(selected);
            main.state().setArrangementState(ArrangementState.DECIDE_ATTENDEES);
        });

        step1.add(availableTimes, BorderLayout.SOUTH);

        // Initialise UI elements for step 2 (specifying number of attendees).
        lStep2 = new JTextPane();
        JPanel step2 = createTextSection(new BorderLayout(), "2. Select Number of Attendees", lStep2, BorderLayout.CENTER);

        // Add a filler panel to split up the input and submission buttons for step 2.
        JPanel step2temp = new JPanel();
        step2temp.setLayout(new GridLayout(1, 2));
        step2temp.setBorder(new EmptyBorder(6, 20, 6, 20));
        tStep2 = new JTextField();
        tStep2.setEnabled(false);
        confirmNumbers = new JButton("Submit");
        confirmNumbers.setEnabled(false);
        confirmNumbers.addActionListener((e) -> {
            Screening selected = main.data().getSelectedScreening();
            Timestamp time = main.state().getSelectedTime();
            int seatsRemaining = main.data().calculateNumberOfAvailableSeats(selected, time);
            if (seatsRemaining == 0) {
                // Show a unique error message for completely sold out time slots.
                JOptionPane.showMessageDialog(null, "Sorry, but unfortunately there are no seats left.\nPlease consider booking on a different time slot.", "Sold Out!", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                int attendees = Integer.parseInt(tStep2.getText());
                // Perform a few validations to make sure the booking can proceed.
                if (attendees < 1 || selected.getVenue().getTotalSeats() < attendees) throw new NumberFormatException();
                if (attendees > seatsRemaining) throw new IndexOutOfBoundsException();
                main.state().setNoOfAttendees(attendees);
                main.state().setArrangementState(ArrangementState.CONFIRM);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Sorry, the input you supplied was invalid.\n" +
                        "Please supply a number between 1 and " + seatsRemaining + "!", "Invalid Input!", JOptionPane.ERROR_MESSAGE);
            } catch (IndexOutOfBoundsException ex) {
                JOptionPane.showMessageDialog(null, "Sorry, but unfortunately there is not enough free seats left.\n" +
                        "Please consider booking on a less populated time slot.", "Not Enough Room!", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        step2temp.add(tStep2);
        step2temp.add(confirmNumbers);
        step2.add(step2temp, BorderLayout.SOUTH);

        // Initialise UI elements for step 3 (confirmation of selection).
        lStep3 = new JTextPane();
        JPanel step3 = createTextSection(new GridLayout(2, 1), "3. Confirmation", lStep3);
        pickSeats = new JButton("Confirm & Select Seats");
        pickSeats.setEnabled(false);
        pickSeats.addActionListener((e) -> main.updateState(MainView.UIState.SEAT_SELECTION));
        step3.add(pickSeats);

        // Initialise UI elements for the miscellaneous panel.
        JPanel options = new JPanel();
        options.setLayout(new BorderLayout());
        options.setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(6, 6, 6, 6),
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY, 1), "Miscellaneous", TitledBorder.CENTER, TitledBorder.TOP)));
        JPanel miscBorder = new JPanel();
        miscBorder.setLayout(new GridLayout(2, 1));
        miscBorder.setBorder(new EmptyBorder(6, 6, 6, 6));
        cancelAll = new JButton("Cancel / Reset Selection");
        cancelAll.setEnabled(false);
        cancelAll.addActionListener((e) -> {
            // Perform a hard reset of this view; un-select a chosen screening.
            main.state().setArrangementState(ArrangementState.UNDECIDED);
            booking.movieGrid().update();
        });
        SpacedJButton viewTickets = new SpacedJButton("View Tickets", 5, 0, 5, 0);
        JButton viewTickets1 = viewTickets.get();
        viewTickets1.addActionListener((e) -> {
            // Get username input before displaying tickets for said username.
            String result = JOptionPane.showInputDialog(null, "Please enter your username:", "View Tickets", JOptionPane.QUESTION_MESSAGE);
            if (result == null) return; // If they clicked cancel
            if (!Pattern.compile("^[a-zA-Z0-9_]{3,16}$").matcher(result).find()) {
                JOptionPane.showMessageDialog(null, "Your selected username is invalid. Please ensure that:\n" +
                        "- It is between 3 and 16 characters long.\n- It only contains letters, numbers, and underscores.", "Invalid Username!", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Find tickets under this valid username.
            List<Ticket> tickets = main.data().findTicketsByUsername(result);
            if (tickets.size() == 0) {
                JOptionPane.showMessageDialog(null, "There are no tickets under this username.", "No Tickets Found", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            main.state().setQueryTickets(tickets);
            main.state().setQueryUsername(result);
            main.updateState(MainView.UIState.TICKET_REVIEW);
        });
        miscBorder.add(cancelAll);
        miscBorder.add(viewTickets);
        options.add(miscBorder);

        mainPanel.add(step1);
        mainPanel.add(step2);
        mainPanel.add(step3);
        mainPanel.add(options);
        reset();
    }

    @Override
    public void reset() {
        // Reset step 1 (picking a date and time) UI elements.
        lStep1.setText("\nPlease select a movie before proceeding with your booking.");
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>(new String[]{"You may not select at the moment."});
        availableTimes.setModel(model);
        availableTimes.setEnabled(false);

        // Reset step 2 (picking number of attendees) UI elements.
        lStep2.setText("\nPlease select a date & time for your booking before entering attendees.");
        tStep2.setEnabled(false);
        tStep2.setText("");
        confirmNumbers.setEnabled(false);

        // Reset step 3 (confirmation) UI elements.
        lStep3.setText("Please enter the number of attendees before selecting your seats.");
        pickSeats.setEnabled(false);

        // Reset miscellaneous elements.
        cancelAll.setEnabled(false);
    }

    @Override
    public void update() {
        switch (main.state().getArrangementState()) {
            case UNDECIDED:
                reset();
                break;
            case DECIDE_WHEN: {
                Screening selected = main.data().getSelectedScreening();
                lStep1.setText("Bookings for '" + selected.getMovie().getName() + " (" + selected.getMovie().getReleaseYear() + ")' are available for " + selected.timeStatus() + ". Please select a time.");
                main.state().setAvailableTimes(main.data().calculateTimes(selected));

                // Convert available times into a more human-friendly format before putting them in the combo box.
                LinkedList<String> friendlyTimes = new LinkedList<>();
                friendlyTimes.add("*please select*");
                for (Timestamp time : main.state().getAvailableTimes())
                    friendlyTimes.add(main.data().friendlyDate(time));

                // Add converted times to combo box for selection.
                DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>(friendlyTimes.toArray(new String[0]));
                availableTimes.setModel(model);
                availableTimes.setEnabled(true);

                // Reset step 2 UI elements just in case they haven't been.
                lStep2.setText("\nPlease select a date & time for your booking before entering attendees.");
                tStep2.setEnabled(false);
                tStep2.setText("");
                confirmNumbers.setEnabled(false);

                // Enable the cancel/reset button at this point, and leave it enabled so that stuff can be reset.
                cancelAll.setEnabled(true);
            }
            break;
            case DECIDE_ATTENDEES: {
                Screening selected = main.data().getSelectedScreening();
                Timestamp time = main.state().getSelectedTime();
                int seatsRemaining = main.data().calculateNumberOfAvailableSeats(selected, time);
                lStep2.setText("Your screening on " + main.data().friendlyDate(time) + " has " + seatsRemaining + " seat(s) remaining. How many do you need?");
                tStep2.setEnabled(true);
                confirmNumbers.setEnabled(true);

                lStep3.setText("Please enter the number of attendees before selecting your seats.");
                pickSeats.setEnabled(false);
            }
            break;
            case CONFIRM: {
                lStep3.setText("You are about to book " + main.state().getNoOfAttendees() + " seat(s) for your chosen time slot. Proceed?");
                pickSeats.setEnabled(true);
            }
        }
    }

    /**
     * Denotes all the different states this view may be in at any given point.
     * StateController keeps a record of such state, and allows this view to display
     * the correct output accordingly.
     */
    public enum ArrangementState {
        UNDECIDED,
        DECIDE_WHEN,
        DECIDE_ATTENDEES,
        CONFIRM
    }
}
