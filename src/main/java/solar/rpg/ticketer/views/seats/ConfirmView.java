package solar.rpg.ticketer.views.seats;

import solar.rpg.ticketer.models.Screening;
import solar.rpg.ticketer.views.MainView;
import solar.rpg.ticketer.views.util.SpacedJButton;
import solar.rpg.ticketer.views.util.View;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.util.regex.Pattern;

/**
 * ConfirmView is preceded by SelectionView, and is the final step in the booking process.
 * Once a user has properly allocated seats to all attendees, they are brought here.
 * The user gets an opportunity to review their choices, and then must enter a username.
 * The booking tickets are stored under a username so that they may be looked at/deleted later.
 *
 * @author Joshua Skinner
 * @version 1.0
 * @see solar.rpg.ticketer.views.booking.TicketView
 * @since 0.1
 */
public class ConfirmView extends View {

    // View Elements
    private JScrollPane scroll;
    private JTextPane details, namePrompt;
    private JTextField nameInput;
    private JButton makeBooking;

    public ConfirmView(MainView main) {
        super(main);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void generate() {
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // This panel sits in the middle of the screen, but does not take up 100% of the width.
        JPanel midPanel = new JPanel();
        midPanel.setLayout(new GridLayout(3, 1));
        midPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY, 1), "Please Review Your Selection", TitledBorder.CENTER, TitledBorder.TOP));

        // Setup elements for the first section (review of details).
        details = new JTextPane();
        scroll = new JScrollPane(details);
        // Enforce a minimum size so the text pane doesn't collapse in on itself under GridBag constraints.
        scroll.setMinimumSize(new Dimension(495, 0));
        details.setEditable(false);
        SimpleAttributeSet JUSTIFIED = (SimpleAttributeSet) ATTRIB_CENTER.clone();
        StyleConstants.setAlignment(JUSTIFIED, StyleConstants.ALIGN_JUSTIFIED);
        details.setParagraphAttributes(JUSTIFIED, true);

        // Setup elements for the second section (enter username).
        namePrompt = new JTextPane();
        namePrompt.setMinimumSize(new Dimension(495, 0));
        JPanel username = createTextSection(new BorderLayout(), "Enter Your Username", namePrompt, BorderLayout.CENTER);

        // Filler panel to split up text field and submission button.
        JPanel userTemp = new JPanel();
        userTemp.setLayout(new GridLayout(1, 2));
        userTemp.setBorder(new EmptyBorder(6, 20, 6, 20));
        nameInput = new JTextField();
        JButton nameConfirm = new JButton("Submit");
        nameConfirm.addActionListener((e) -> {
            String name = nameInput.getText();

            // Test username against regular expression to ensure validity.
            if (!Pattern.compile("^[a-zA-Z0-9_]{3,16}$").matcher(name).find()) {
                JOptionPane.showMessageDialog(null, "Your selected username is invalid. Please ensure that:\n" +
                        "- It is between 3 and 16 characters long.\n- It only contains letters, numbers, and underscores.", "Invalid Username!", JOptionPane.ERROR_MESSAGE);
                main.state().setBookingUsername("");
                reset();
                return;
            }

            namePrompt.setText("\nYour tickets will be saved under the username:\n\"" + name + "\"\n" +
                    "To complete your booking, please click \"Make Booking\"!");
            main.state().setBookingUsername(name);
            makeBooking.setEnabled(true);
        });
        userTemp.add(nameInput);
        userTemp.add(nameConfirm);
        username.add(userTemp, BorderLayout.SOUTH);

        // Setup elements for the control section (confirm booking, go back, and cancel booking)
        JPanel controls = new JPanel();
        controls.setLayout(new GridLayout(3, 1));
        SpacedJButton makeBooking = new SpacedJButton("Make Booking", 10, 10, 10, 10);
        this.makeBooking = makeBooking.get();
        this.makeBooking.setBackground(new Color(183, 237, 147));
        this.makeBooking.setEnabled(false);
        this.makeBooking.addActionListener((e) -> {
            // Make sure the user can't spam the button while saving a booking.
            if (!this.makeBooking.isEnabled()) return;
            this.makeBooking.setEnabled(false);
            main.data().compileBooking();
            main.updateState(MainView.UIState.INITIAL_BOOKING);
        });
        SpacedJButton goBack = new SpacedJButton("Back to Seat Selection", 10, 10, 10, 10);
        goBack.get().addActionListener((e) -> main.updateState(MainView.UIState.SEAT_SELECTION));
        SpacedJButton cancel = new SpacedJButton("Cancel Booking", 10, 10, 10, 10);
        cancel.get().addActionListener((e) -> main.updateState(MainView.UIState.INITIAL_BOOKING));
        controls.add(makeBooking);
        controls.add(goBack);
        controls.add(cancel);

        midPanel.add(scroll);
        midPanel.add(username);
        midPanel.add(controls);

        // Apply grid bag constraints to horizontally centre the view.
        GridBagConstraints grid = new GridBagConstraints();
        grid.weightx = 1 / 6D;
        grid.weighty = 1;
        grid.fill = GridBagConstraints.BOTH;
        grid.gridx = 0;
        mainPanel.add(new JPanel(), grid);
        grid.weightx = 2 / 3D;
        grid.gridx = 1;
        mainPanel.add(midPanel, grid);
        grid.weightx = 1 / 6D;
        grid.gridx = 2;
        mainPanel.add(new JPanel(), grid);
    }

    @Override
    public void reset() {
        namePrompt.setText("\n\nPlease enter a username to save the tickets under so you may return to them later.");
        nameInput.setText("");
        makeBooking.setEnabled(false);
    }

    @Override
    public void update() {
        Screening selected = main.data().getSelectedScreening();
        details.setText("Please confirm your selections:\n" +
                "Screening: " + selected.getMovie().getName() + " (" + selected.getMovie().getReleaseYear() + ")\n" +
                "Running Time: " + selected.getMovie().getRunningTime() + " minutes\n" +
                "On " + main.data().friendlyDate(main.state().getSelectedTime()) + " at Venue #" + selected.getVenue().getVenueNum() + "\n" +
                "Number of Attendees: " + main.state().getNoOfAttendees() + "\n" +
                "Selected Seats: " + main.state().selectedSeatsAsString());
        SwingUtilities.invokeLater(() -> scroll.getVerticalScrollBar().setValue(0));
    }
}
