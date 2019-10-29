package solar.rpg.ticketer.views.booking;

import solar.rpg.ticketer.models.Movie;
import solar.rpg.ticketer.models.Ticket;
import solar.rpg.ticketer.views.MainView;
import solar.rpg.ticketer.views.util.SpacedJButton;
import solar.rpg.ticketer.views.util.View;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.List;

public class TicketView extends View {

    // View Elements
    private JScrollPane scroll;
    private JPanel midPanel, currentTickets;
    private JButton goBack;

    public TicketView(MainView main) {
        super(main);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void generate() {
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        midPanel = new JPanel();
        midPanel.setLayout(new BorderLayout());

        currentTickets = new JPanel();
        currentTickets.setLayout(new GridBagLayout());
        scroll = new JScrollPane(currentTickets);
        scroll.setMaximumSize(new Dimension(495, Integer.MAX_VALUE));
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        SpacedJButton goBack = new SpacedJButton("Return to Booking Area", 10, 10, 10, 10);
        this.goBack = goBack.get();
        this.goBack.addActionListener((e) -> main.updateState(MainView.UIState.INITIAL_BOOKING));

        midPanel.add(scroll);
        midPanel.add(goBack, BorderLayout.SOUTH);

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
        // This view does not require a reset.
    }

    @Override
    public void update() {
        midPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY, 1),
                "Tickets Under \"" + main.state().getQueryUsername() + "\"", TitledBorder.CENTER, TitledBorder.TOP));
        currentTickets.removeAll();

        // Movies mapped to lists of tickets.
        HashMap<Movie, List<Ticket>> remapped = main.data().remapTickets();

        // Setup grid bag constraints.
        GridBagConstraints grid = new GridBagConstraints();
        grid.fill = GridBagConstraints.HORIZONTAL;
        grid.gridx = 0;
        grid.gridy = 0;
        grid.weightx = 1;
        grid.weighty = 0.001;
        grid.anchor = GridBagConstraints.NORTHWEST;

        // Add each movie section individually.
        remapped.forEach((key, value) -> {
            currentTickets.add(new MovieTicketsDisplay(key, value), grid);
            grid.gridy++;
        });

        // Position scrollbar up top.
        SwingUtilities.invokeLater(() -> scroll.getVerticalScrollBar().setValue(0));
    }

    private class MovieTicketsDisplay extends JPanel {

        MovieTicketsDisplay(Movie movie, List<Ticket> tickets) {
            // Create border for this movie section.
            setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(5, 5, 5, 5),
                    BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY, 1),
                            "Tickets for " + movie.getName() + " (" + movie.getReleaseYear() + ")", TitledBorder.CENTER, TitledBorder.TOP)));
            setLayout(new GridBagLayout());

            GridBagConstraints outerGrid = new GridBagConstraints();
            outerGrid.weightx = 1;
            outerGrid.ipady = 3;
            outerGrid.fill = GridBagConstraints.HORIZONTAL;
            outerGrid.anchor = GridBagConstraints.NORTHWEST;

            SpacedJButton deleteAll = new SpacedJButton("Delete All", 0, 0, 0, 5);
            deleteAll.get().setBackground(new Color(227, 102, 89));
            deleteAll.get().addActionListener((e) -> {
                int decision = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete all tickets for this movie?", "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (decision == 0) {
                    main.data().deleteTickets(tickets);
                    JOptionPane.showMessageDialog(null, tickets.size() + " ticket(s) have been deleted.", "Tickets Deleted", JOptionPane.INFORMATION_MESSAGE);
                    hideSelf();
                }
            });

            tickets.forEach(ticket -> {
                // Setup the three UI attributes for each ticket.
                JPanel ticketPanel = new JPanel();
                ticketPanel.setLayout(new GridBagLayout());

                JTextPane info = new JTextPane();
                info.setEditable(false);
                info.setText("Date: " + main.data().friendlyDate(ticket.getSelectedDate()) + "\nSeat: " + ticket.getAllocatedSeat());
                SpacedJButton show = new SpacedJButton("Show", 0, 5, 0, 5);
                show.get().addActionListener((e) ->
                        // Show all the information on this ticket.
                        JOptionPane.showMessageDialog(null, "Information on this ticket:\n" +
                        "Screening: " + movie.getName() + " (" + movie.getReleaseYear() + ")\n" +
                        "Running time: " + movie.getRunningTime() + " minutes\n" +
                        "Time slot: " + main.data().friendlyDate(ticket.getSelectedDate()) + "\n" +
                        "Showing at Venue: Venue #" + ticket.getScreening().getVenue().getVenueNum() +
                        " (" + ticket.getScreening().getVenue().getTotalSeats() + " seats available)\n" +
                        "Seat allocation: Seat " + ticket.getAllocatedSeat() + "\n" +
                        "Booked by username: \"" + ticket.getUsername() + "\""));
                SpacedJButton delete = new SpacedJButton("Delete", 0, 5, 0, 5);
                delete.get().addActionListener((e) -> {
                    int decision = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this ticket?", "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (decision == 0) {
                        main.data().deleteTicket(ticket);
                        JOptionPane.showMessageDialog(null, "This ticket has been deleted.", "Ticket Deleted", JOptionPane.INFORMATION_MESSAGE);
                        tickets.remove(ticket);

                        // Hide row if there are still other tickets, otherwise hide entire panel.
                        if (tickets.size() == 0)
                            hideSelf();
                        else {
                            ticketPanel.setVisible(false);
                            ticketPanel.setEnabled(false);
                        }
                    }
                });

                // Setup constraints for each of the three UI elements.
                GridBagConstraints innerGrid = new GridBagConstraints();
                innerGrid.fill = GridBagConstraints.HORIZONTAL;
                innerGrid.weightx = 0.5;
                innerGrid.gridx = 0;
                ticketPanel.add(info, innerGrid);
                innerGrid.weightx = 0.25;
                innerGrid.gridx = 1;
                ticketPanel.add(show, innerGrid);
                innerGrid.gridx = 2;
                ticketPanel.add(delete, innerGrid);

                // Add to list of tickets.
                outerGrid.gridy++;
                add(ticketPanel, outerGrid);
            });

            // Add button last.
            outerGrid.anchor = GridBagConstraints.SOUTHEAST;
            outerGrid.fill = GridBagConstraints.NONE;
            outerGrid.weightx = 0.25;
            outerGrid.gridy++;
            add(deleteAll, outerGrid);
        }

        /**
         * Once all tickets have been deleted from a section, simply hide the panel.
         */
        void hideSelf() {
            setVisible(false);
            setEnabled(false);
        }
    }
}
