package solar.rpg.ticketer.views.booking;

import solar.rpg.ticketer.models.Screening;
import solar.rpg.ticketer.views.IView;
import solar.rpg.ticketer.views.MainView;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.sql.Timestamp;
import java.util.LinkedList;

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
public class ArrangementView implements IView {

    // Reference to MainView & BookingView
    private MainView main;
    private BookingView booking;

    // View elements.
    private JPanel backPanel;
    private JTextPane lStep1, lStep2, lStep3;
    private JTextField tStep2;
    private JButton cancelAll, seeExisting, confirmNumbers, pickSeats;
    private JComboBox<String> availableTimes;

    ArrangementView(MainView main, BookingView booking) {
        this.main = main;
        this.booking = booking;
        generate();
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void generate() {
        // Set some styling attributes for the text panes.
        SimpleAttributeSet attribs = new SimpleAttributeSet();
        StyleConstants.setAlignment(attribs, StyleConstants.ALIGN_CENTER);
        StyleConstants.setFontSize(attribs, 14);

        // Create a back panel with an empty border to emulate padding & margins.
        backPanel = new JPanel();
        backPanel.setLayout(new GridLayout(4, 1));
        backPanel.setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(6, 6, 6, 6), BorderFactory.createLineBorder(Color.GRAY, 1)));

        // Initialise UI elements for step 1 (picking a date and time).
        JPanel step1 = new JPanel();
        step1.setLayout(new BorderLayout());
        step1.setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(6, 6, 6, 6),
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY, 1), "1. Select Date & Time", TitledBorder.CENTER, TitledBorder.TOP)));
        lStep1 = new JTextPane();
        lStep1.setEditable(false);
        lStep1.setParagraphAttributes(attribs, true);
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

        step1.add(lStep1, BorderLayout.CENTER);
        step1.add(availableTimes, BorderLayout.SOUTH);

        // Initialise UI elements for step 2 (specifying number of attendees).
        JPanel step2 = new JPanel();
        step2.setLayout(new BorderLayout());
        step2.setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(6, 6, 6, 6),
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY, 1), "2. Select Number of Attendees", TitledBorder.CENTER, TitledBorder.TOP)));
        lStep2 = new JTextPane();
        lStep2.setEditable(false);
        lStep2.setParagraphAttributes(attribs, true);

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
                JOptionPane.showMessageDialog(null, "Sorry, but unfortunately there are not seats left.\nPlease consider booking on a different time slot.", "Sold Out!", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                int attendees = Integer.parseInt(tStep2.getText());
                // Intentionally throw exceptions for invalid input so validation dialog can be displayed.
                if (attendees < 1 || selected.getVenue().getTotalSeats() < attendees) throw new NumberFormatException();
                if (attendees > seatsRemaining) throw new IndexOutOfBoundsException();
                main.state().setNoOfAttendees(attendees);
                main.state().setArrangementState(ArrangementState.CONFIRM);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Sorry, the input you supplied was invalid.\nPlease supply a number between 1 and " + seatsRemaining + "!", "Invalid Input!", JOptionPane.ERROR_MESSAGE);
            } catch (IndexOutOfBoundsException ex) {
                JOptionPane.showMessageDialog(null, "Sorry, but unfortunately there is not enough free seats left.\nPlease consider booking on a less populated time slot.", "Not Enough Room!", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        step2temp.add(tStep2);
        step2temp.add(confirmNumbers);

        step2.add(lStep2, BorderLayout.CENTER);
        step2.add(step2temp, BorderLayout.SOUTH);

        // Initialise UI elements for step 3 (confirmation of selection).
        JPanel step3 = new JPanel();
        step3.setLayout(new GridLayout(2, 1));
        step3.setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(6, 6, 6, 6),
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY, 1), "3. Confirmation", TitledBorder.CENTER, TitledBorder.TOP)));
        lStep3 = new JTextPane();
        lStep3.setEditable(false);
        lStep3.setParagraphAttributes(attribs, true);
        pickSeats = new JButton("Confirm & Select Seats");
        pickSeats.setEnabled(false);
        step3.add(lStep3);
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
            main.state().setSelectedScreening(-1);
            main.state().setArrangementState(ArrangementState.UNDECIDED);
            booking.movieGrid().update();
        });
        miscBorder.add(cancelAll);
        options.add(miscBorder);

        backPanel.add(step1);
        backPanel.add(step2);
        backPanel.add(step3);
        backPanel.add(options);
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
            }
            break;
            case CONFIRM: {
                lStep3.setText("You are about to book " + main.state().getNoOfAttendees() + " seat(s) for your chosen time slot. Proceed?");
                pickSeats.setEnabled(true);
            }
        }
    }

    @Override
    public JPanel getPanel() {
        return backPanel;
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
