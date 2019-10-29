package solar.rpg.ticketer.views.booking;

import solar.rpg.ticketer.models.Screening;
import solar.rpg.ticketer.views.IView;
import solar.rpg.ticketer.views.MainView;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.util.LinkedList;

/**
 * MovieGridView is a sub-view of BookingView.
 * This view allows the user to view a grid of 6 screenings at a time.
 * The user can navigate to different "pages" of grids using the buttons on the bottom.
 *
 * @author Joshua Skinner
 * @version 1.0
 * @since 0.1
 */
public class MovieGridView implements IView {

    // Reference to MainView & BookingView
    private MainView main;
    private BookingView booking;

    // View elements.
    private JPanel backPanel;
    private JPanel moviePanel;
    private MovieSquare[] grid = new MovieSquare[6];
    private JButton prevPage, indicator, nextPage;

    public MovieGridView(MainView main, BookingView booking) {
        this.main = main;
        this.booking = booking;

        generate();
    }

    @Override
    public void generate() {
        // The back panel houses both the grid layout and the navigation buttons.
        backPanel = new JPanel();
        backPanel.setLayout(new BorderLayout());

        // Initialise the grid panel and the individual 6 squares.
        moviePanel = new JPanel();
        moviePanel.setLayout(new GridLayout(3, 2));
        for (int i = 0; i < grid.length; i++) {
            MovieSquare square = new MovieSquare();
            grid[i] = square;
            moviePanel.add(square);
        }

        // Initialise the pagination panel and its UI elements.
        JPanel paginationPanel = new JPanel();
        paginationPanel.setLayout(new GridLayout(1, 3));
        paginationPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        prevPage = new JButton("Previous");
        indicator = new JButton("");
        indicator.setEnabled(false);
        nextPage = new JButton("Next");
        paginationPanel.add(prevPage);
        paginationPanel.add(indicator);
        paginationPanel.add(nextPage);

        // Setup click listeners for the previous and next buttons.
        prevPage.addActionListener((l) -> {
            if (main.state().getSelectedPage() == 1) main.state().setSelectedPage(main.state().getMaxPage());
            else main.state().setSelectedPage(main.state().getSelectedPage() - 1);
            update();
        });
        nextPage.addActionListener((l) -> {
            if (main.state().getSelectedPage() == main.state().getMaxPage()) main.state().setSelectedPage(1);
            else main.state().setSelectedPage(main.state().getSelectedPage() + 1);
            update();
        });

        // Refresh everything so it looks normal.
        backPanel.add(moviePanel, BorderLayout.CENTER);
        backPanel.add(paginationPanel, BorderLayout.SOUTH);
        update();
    }

    /**
     * This little class represents one of the six squares shown in the grid.
     * It contains three sections: a title, subtitle, and booking button.
     */
    private class MovieSquare extends JPanel {

        // UI elements in an individual square.
        private JTextPane titleText;
        private JTextPane subtitleText;
        private JButton book;

        // Logical elements
        private int screeningID = -1;

        MovieSquare() {
            // Set some styling attributes for the text panes.
            SimpleAttributeSet attribs = new SimpleAttributeSet();
            StyleConstants.setAlignment(attribs, StyleConstants.ALIGN_CENTER);
            StyleConstants.setFontSize(attribs, 14);

            // Apply a grid layout and a border to achieve the grid/square effect.
            setLayout(new GridLayout(3, 0));
            setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(6, 6, 6, 6), BorderFactory.createLineBorder(Color.GRAY, 1)));

            // Initialise title pane.
            titleText = new JTextPane();
            titleText.setParagraphAttributes(attribs, true);
            titleText.setEditable(false);

            // Initialise subtitle pane.
            subtitleText = new JTextPane();
            StyleConstants.setFontSize(attribs, 12);
            StyleConstants.setItalic(attribs, true);
            subtitleText.setParagraphAttributes(attribs, true);
            subtitleText.setEditable(false);

            // Initialise booking button.
            book = new JButton("");
            book.addActionListener((e) -> {
                main.state().setSelectedScreening(screeningID);
                MovieGridView.this.update();
                main.state().setArrangementState(ArrangementView.ArrangementState.DECIDE_WHEN);
            });
            add(titleText);
            add(subtitleText);
            add(book);
            none();
        }

        /**
         * Displays information about, and tracks a given screening.
         *
         * @param screening The screening to display.
         */
        void display(Screening screening) {
            setVisible(true);
            titleText.setText(screening.getMovie().getName());
            subtitleText.setText(screening.getMovie().getGenre() + " (" + screening.getMovie().getRunningTime() + " mins), from " + screening.getMovie().getReleaseYear());
            screeningID = screening.getID();

            if (main.state().getSelectedScreening() == screeningID) {
                book.setText("Selected!");
                book.setEnabled(false);
            } else {
                book.setText("Book Now (Venue " + screening.getVenue().getVenueNum() + ")");
                book.setEnabled(true);
            }
        }

        /**
         * Hides the square if there is nothing to show.
         * Stops keeping track of a screening.
         */
        void none() {
            setVisible(false);
            screeningID = -1;
        }
    }

    @Override
    public void update() {
        // Calculate & apply pagination results for selected page.
        LinkedList<Screening> page = main.state().paginate();
        for (int i = 0; i < grid.length; i++) {
            if (i >= page.size()) grid[i].none();
            else grid[i].display(page.get(i));
        }
        // Update page indicator.
        indicator.setText("Page " + main.state().getSelectedPage() + " of " + main.state().getMaxPage());
    }

    @Override
    public void reset() {
        main.state().setSelectedPage(1);
    }

    @Override
    public JPanel getPanel() {
        return backPanel;
    }
}
