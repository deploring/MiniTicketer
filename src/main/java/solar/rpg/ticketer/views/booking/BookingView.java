package solar.rpg.ticketer.views.booking;

import solar.rpg.ticketer.views.MainView;
import solar.rpg.ticketer.views.util.View;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * BookingView encompasses two smaller sub-views: MovieGridView & ArrangementView.
 * MovieGridView allows the user to navigate a grid of squares that represent currently screening movies.
 * ArrangementView takes a screening movie from the previous view and starts booking arrangements,
 * such as the preferred time slot and number of attendees.
 * <p>
 * Overall, both seek to accomplish a flow of information such that after all of this data is defined,
 * the user can then select where each attendee will sit at the venue where the screening takes place.
 * This is however, done on a separate view, and is orchestrated by BookingView; the "main" view.
 *
 * @author Joshua Skinner
 * @version 1.0
 * @see BookingView
 * @see ArrangementView
 * @since 0.1
 */
public class BookingView extends View {

    // View Elements
    private View movieGrid, arrangement;

    public BookingView(MainView main) {
        super(main);
    }

    @Override
    public void generate() {
        // Generate labelled border.
        mainPanel.setLayout(new GridLayout(1, 2));
        mainPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY, 1),
                "Book Your Tickets Below!", TitledBorder.CENTER, TitledBorder.TOP));

        // Add both secondary views.
        movieGrid = new MovieGridView(main);
        arrangement = new ArrangementView(main, this);

        mainPanel.add(movieGrid.getPanel());
        mainPanel.add(arrangement.getPanel());
    }

    @Override
    public void reset() {
        movieGrid.reset();
        arrangement.reset();
    }

    @Override
    public void update() {
        movieGrid.update();
        arrangement.update();
    }

    /* References to instances of the sub-views. */

    public MovieGridView movieGrid() {
        return (MovieGridView) movieGrid;
    }

    public ArrangementView arrangement() {
        return (ArrangementView) arrangement;
    }
}
