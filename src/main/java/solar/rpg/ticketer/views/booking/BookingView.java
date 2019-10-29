package solar.rpg.ticketer.views.booking;

import solar.rpg.ticketer.views.IView;
import solar.rpg.ticketer.views.MainView;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class BookingView implements IView {

    // Reference to MainView
    private MainView main;

    // View Elements
    private JPanel bookingPanel;
    private IView movieGrid, arrangement;

    BookingView(MainView main) {
        this.main = main;

        // Generate View elements.
        generate();
    }

    @Override
    public void generate() {
        // Generate labelled border.
        bookingPanel = new JPanel();
        bookingPanel.setLayout(new GridLayout(1, 2));
        bookingPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY, 1),
                "Book Your Tickets Below!", TitledBorder.CENTER, TitledBorder.TOP));

        // Add both secondary views.
        movieGrid = new MovieGridView(main, this);
        arrangement = new ArrangementView(main, this);
        bookingPanel.add(movieGrid.getPanel());
        bookingPanel.add(arrangement.getPanel());
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

    @Override
    public JPanel getPanel() {
        return bookingPanel;
    }

    public MovieGridView movieGrid() {
        return (MovieGridView) movieGrid;
    }

    public ArrangementView arrangement() {
        return (ArrangementView) arrangement;
    }
}
