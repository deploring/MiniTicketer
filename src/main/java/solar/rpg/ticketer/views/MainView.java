package solar.rpg.ticketer.views;

import solar.rpg.ticketer.controller.DataController;
import solar.rpg.ticketer.controller.StateController;
import solar.rpg.ticketer.views.booking.ArrangementView;
import solar.rpg.ticketer.views.booking.BookingView;
import solar.rpg.ticketer.views.seats.ConfirmView;
import solar.rpg.ticketer.views.seats.SelectionView;
import solar.rpg.ticketer.views.util.View;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.Set;

/**
 * This JFrame controls each individual sub-view on the screen.
 * It also acts as the central class where models, views, and
 * controllers can all communicate with one another.
 *
 * @author Joshua Skinner
 * @author Keagan Foster
 * @version 1.0
 * @since 0.1
 */
public final class MainView extends JFrame {

    // Instances of the two main controllers.
    private DataController dataController;
    private StateController stateController;

    // Instances of each potential view.
    private View bookingView, selectionView, confirmView;

    // Menu bar UI elements.
    private JCheckBoxMenuItem[] sortGenres;
    private JMenu sort;

    public MainView() {
        super("MiniTicketer - v0.1");

        // Initialise dataController.
        dataController = new DataController(this);
        stateController = new StateController(this);

        getContentPane().setLayout(new BorderLayout());
        createUI();
    }

    /**
     * Initialises all UI components.
     */
    private void createUI() {
        setupMenuBar();
        bookingView = new BookingView(this);
        selectionView = new SelectionView(this);
        confirmView = new ConfirmView(this);
        updateState(UIState.INITIAL_BOOKING);
    }

    /**
     * Sets up a basic JMenu bar with some helpful information & tools.
     * Amongst them is a menu that allows the user to sort the
     */
    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // Create a few helpful menus.
        JMenu help = new JMenu("Help");
        JMenuItem guide = new JMenuItem("Quick Start Guide");
        JMenuItem context = new JMenuItem("Context");
        JMenuItem source = new JMenuItem("Source & Planning");
        guide.addActionListener((e) -> JOptionPane.showMessageDialog(null, "Welcome to Mini-Ticketer! Yes, that's a word.\nHere, you are able to view a wide array of short-films of varying genres.\nAre you interested in perhaps watching one of them? Click on one to book now!\nSpecify the date, time, and number of attendees. Please pick your venue seats.\nOnce you're ready to book your tickets, pick a username to save them under!\nGood luck, and happy watching! - Mini-Ticketer", "Welcome to Mini-Ticketer!", JOptionPane.INFORMATION_MESSAGE));
        context.addActionListener((e) -> JOptionPane.showMessageDialog(null, "This program was developed as part of an employer assessment.\nIt is intended to show off my OOP design skills with a focus on:\na. having a data-centric context; and\nb. maintaining high quality code throughout development.\nThank you!", "About This Program", JOptionPane.INFORMATION_MESSAGE));
        source.addActionListener((e) -> JOptionPane.showMessageDialog(null, "This project's code, along with all design documents are available at:\nhttps://github.com/lavuh/MiniTicketer - Please do not hesitate to ask questions!", "Source Code & Design", JOptionPane.INFORMATION_MESSAGE));
        help.add(guide);
        help.add(context);
        help.add(source);

        // Create a menu that allows the user to select what genres to see in the grid.
        sort = new JMenu("Sort Movies");
        // Only allow the user to select genres that have some screenings on them.
        Set<Map.Entry<String, Integer>> genres = dataController.getCurrentGenreSet();
        sortGenres = new JCheckBoxMenuItem[genres.size()];
        for (Map.Entry<String, Integer> entry : genres) {
            JCheckBoxMenuItem genreItem = new JCheckBoxMenuItem(entry.getKey());
            sortGenres[entry.getValue()] = genreItem;
            genreItem.addActionListener((e) -> {
                // Only allow one item to be selected at a time; so uncheck others if applicable.
                if (genreItem.isSelected()) {
                    // This genre is now the sorting target.
                    clearSortedGenres();
                    genreItem.setState(true);
                    state().changeSortBy(entry.getKey());
                } else state().changeSortBy(""); // Sorting has been disabled.
            });
            sort.add(genreItem);
        }
        menuBar.add(help);
        menuBar.add(sort);
        setJMenuBar(menuBar);
    }

    /**
     * Completely clears selection preference for genre sorting.
     */
    private void clearSortedGenres() {
        for (JCheckBoxMenuItem item : sortGenres)
            item.setState(false);
    }

    /**
     * Enables or disables the sorting menu.
     * It should only be enabled when the user is on the "Booking" view.
     *
     * @param enabled True if sorting menu should be enabled, otherwise false.
     */
    private void setSort(boolean enabled) {
        sort.setEnabled(enabled);

        // When disabled, also clear any sortBy values.
        if (!enabled) {
            clearSortedGenres();
            state().changeSortBy("");
        }
    }

    /**
     * Changes what view is being shown on the screen.
     *
     * @param state What should be shown?
     */
    public void updateState(UIState state) {
        switch (state) {
            case INITIAL_BOOKING:
                stateController.setArrangementState(ArrangementView.ArrangementState.UNDECIDED);
                booking().movieGrid().update();
                setSort(true);
                swap(bookingView.getPanel());
                break;
            case SEAT_SELECTION:
                setSort(false);
                selectionView.update();
                swap(selectionView.getPanel());
                break;
            case CONFIRMATION:
                confirmView.reset();
                confirmView.update();
                swap(confirmView.getPanel());
                break;
            case TICKET_REVIEW:
                break;
        }
    }

    /**
     * Clears the content panel and allows a new view to be displayed.
     *
     * @param panel The panel to show in the empty space.
     */
    private void swap(JPanel panel) {
        // Remove everything from the content panel.
        getContentPane().removeAll();

        getContentPane().add(panel, BorderLayout.CENTER);

        // Revalidate the frame and content pane.
        revalidate();
        repaint();
        getContentPane().revalidate();
        getContentPane().repaint();
    }

    /**
     * Enumerated type that defines the finite
     * amount of states the application may be in.
     */
    public enum UIState {
        INITIAL_BOOKING,
        SEAT_SELECTION,
        CONFIRMATION,
        TICKET_REVIEW
    }

    /* References to instances of the controllers. */

    public DataController data() {
        return dataController;
    }

    public StateController state() {
        return stateController;
    }

    /* References to instances of the views. */

    public BookingView booking() {
        return (BookingView) bookingView;
    }

    public SelectionView selection() {
        return (SelectionView) selectionView;
    }
}
